package ar.edu.itba.it.pdc.proxy.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	
	public Socket socks5connection(final ByteStreamsInfo bsi, int timeout) {
		FutureTask<Socket> futureTask = new FutureTask<Socket>(new Callable<Socket>() {
			public Socket call() throws Exception {
				Isecu.log.debug("Comienza socket");
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

		svc.execute(futureTask);
		
		try {
			return futureTask.get(timeout, TimeUnit.MILLISECONDS);
		}catch (Exception e) {
			Isecu.log.debug("Timeout", e);
			throw new InvalidProtocolException("Timeout SOCKS5 connection.");
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
        
        Isecu.log.debug("Response: " + response[0] + "" + response[1]);
        
        if (response[0] != (byte) 0x05 || response[1] != (byte) 0x00) {
            return false;
        }

        byte[] connectionRequest = getSocks5ConnectCmd(bsi.getSid() + bsi.getJid() + bsi.getTo());
        byte[] connectionResponse;
        try {
			 connectionResponse = receiveSocks5Message(in);
		} catch (FileTransferException e) {
			Isecu.log.debug(e);
			return false;
		}
		
		connectionRequest[1] = (byte) 0x00;
        return Arrays.equals(connectionRequest, connectionResponse);
	}
	
		
	private byte[] receiveSocks5Message(DataInputStream in) throws IOException, FileTransferException {
        byte[] header = new byte[5];
        in.readFully(header, 0, 5);

        if (header[3] != (byte) 0x03) {
            throw new FileTransferException("Unsupported SOCKS5 address type");
        }

        int addressLength = header[4];

        byte[] response = new byte[7 + addressLength];
        System.arraycopy(header, 0, response, 0, header.length);

        in.readFully(response, header.length, addressLength + 2);

        return response;
    }
        
        
	
	private byte[] getSocks5ConnectCmd(String addr) {
		byte[] hashAddr = hash(addr);
        
        byte[] cmd = new byte[7 + hashAddr.length + 2];
        cmd[0] = (byte) 0x05; //Socks version
        cmd[1] = (byte) 0x01; //Connect
        cmd[2] = (byte) 0x00; //Reserved byte
        cmd[3] = (byte) 0x03; //Address type
        cmd[4] = (byte) hashAddr.length;
        System.arraycopy(hashAddr, 0, cmd, 5, hashAddr.length);
        cmd[hashAddr.length] = 0;		// \
        cmd[hashAddr.length + 1] = 0;	// Port 0

        return cmd;
	}
	
	private byte[] hash(String target) {
		byte[] ret;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(target.getBytes());
			ret = md.digest();
		} catch (NoSuchAlgorithmException e) {
			Isecu.log.debug(e);
			Isecu.log.fatal("SHA1");
			return null;
		}
		return ret;
	}

}
