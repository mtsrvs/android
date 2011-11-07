package ar.edu.itba.it.pdc.proxy.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.exception.FileTransferException;
import ar.edu.itba.it.pdc.exception.InvalidProtocolException;

@Component
public class FileTransferManager {
	
	ExecutorService svc = Executors.newCachedThreadPool();
	
	public FileTransferManager() {
	}
	
	public Socket socks5connect(final ByteStreamsInfo bsi, int timeout) throws FileTransferException {
		FutureTask<Socket> futureTask = new FutureTask<Socket>(new Callable<Socket>() {
			public Socket call() throws Exception {
				Socket socket = new Socket();
				
				SocketAddress socketAddress = new InetSocketAddress(bsi.getHost(), Integer.valueOf(bsi.getPort()));
				socket.connect(socketAddress);
				
				if(!establish(socket, bsi)) {
					socket.close();
					throw new InvalidProtocolException("Failed establishins SOCKS5 connection");
				}
				
				return socket;
			}
		});

		try {
			svc.execute(futureTask);
			return futureTask.get(timeout, TimeUnit.MILLISECONDS);
		}catch(Exception e) {
			throw new FileTransferException(e.getMessage());
		}
		
	}
	
	private boolean establish(Socket socket, ByteStreamsInfo bsi) throws IOException {
		Isecu.log.debug("Establish");
		DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
     // authentication negotiation
        byte[] cmd = new byte[3];

        cmd[0] = (byte) 0x05; // Socks  5
        cmd[1] = (byte) 0x01; // Auth methods
        cmd[2] = (byte) 0x00; // No-Auth
        
        out.write(cmd);
        out.flush();
        
        byte[] response = new byte[2];
        in.readFully(response);
        
        if (response[0] != (byte) 0x05 || response[1] != (byte) 0x00) {
        	Isecu.log.debug("No-authentication unsopported");
            return false;
        }

        byte[] connectionRequest = getSocks5ConnectCmd(bsi);
        out.write(connectionRequest);
        out.flush();
        
        byte[] connectionResponse;
        try {
			 connectionResponse = receiveSocks5Message(in);
		} catch (Exception e) {
			Isecu.log.debug("Read response", e);
			return false;
		}
		
		connectionRequest[1] = (byte) 0x00;
        return Arrays.equals(connectionRequest, connectionResponse);
	}
	
		
	private byte[] receiveSocks5Message(DataInputStream in) throws IOException, FileTransferException {
		Isecu.log.debug("Llega al receive");
		byte[] header = new byte[5];
		
        in.readFully(header, 0, 5);

        if (header[3] != (byte) 0x03) {
        	Isecu.log.debug("Unsupported SOCKS5 address type");
            throw new FileTransferException("Unsupported SOCKS5 address type");
        }

        int addressLength = header[4];

        byte[] response = new byte[7 + addressLength];
        System.arraycopy(header, 0, response, 0, header.length);

        in.readFully(response, header.length, addressLength + 2);

        Isecu.log.debug("Sale en Socks5 response");
        return response;
    }
        
        
	
	private byte[] getSocks5ConnectCmd(ByteStreamsInfo bsi) {
		StringBuilder addr = new StringBuilder();
		addr.append(bsi.getSid()).append(bsi.getFrom()).append(bsi.getTo());
		byte[] hashAddr = hash(addr.toString()).getBytes();
        
        byte[] cmd = new byte[7 + hashAddr.length];
        cmd[0] = (byte) 0x05; //Socks version
        cmd[1] = (byte) 0x01; //Connect
        cmd[2] = (byte) 0x00; //Reserved byte
        cmd[3] = (byte) 0x03; //Address type
        cmd[4] = (byte) hashAddr.length;
        System.arraycopy(hashAddr, 0, cmd, 5, hashAddr.length);
        //Port 0
        cmd[cmd.length - 2] = 0;
        cmd[cmd.length - 1] = 0;	

        return cmd;
	}
	
	private String hash(String target) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(target.getBytes("UTF-8"));
			byte[] ret = md.digest();
			return encodeHex(ret);
		} catch (Exception e) {
			Isecu.log.debug(e);
			Isecu.log.fatal("Hash error");
			throw new InvalidProtocolException("Hash error");
		}
	}
	
	private String encodeHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            if (((int) aByte & 0xff) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toString((int) aByte & 0xff, 16));
        }

        return hex.toString();
    }

}
