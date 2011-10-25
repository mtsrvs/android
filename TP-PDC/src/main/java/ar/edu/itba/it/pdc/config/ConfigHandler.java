package ar.edu.itba.it.pdc.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.handlers.TCPHandler;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;

@Component
public class ConfigHandler implements TCPHandler {

	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	private ConfigUtils configUtils;
	private ByteBuffer request;
	private String response = "";
	private HandlerState state;
	
	private enum HandlerState {
		AUTH_REQUIRED,
		PASS_REQUIRED,
		AUTHORIZED,
		LOGGED_OUT
	};
	
	private enum Commands {
		USER,
		PASS,
		LOGOUT,
		QUIT,
		ADDBLACKIP,
		REMOVEBLACKIP
	};
	
	@Autowired
	public ConfigHandler(ConfigLoader configLoader, ConnectionMap connectionMap, ConfigUtils configUtils) {
		this.configLoader = configLoader;
		this.connectionMap = connectionMap;
		this.configUtils = configUtils;
		request = ByteBuffer.allocate(configLoader.getBufferSize());
		state = HandlerState.AUTH_REQUIRED;
	}
	
	public void read(SelectionKey key, SelectionKey endPointKey)
			throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        
        request.clear();
        long bytesRead = sc.read(request);
        buf.clear();
        
        if (bytesRead == -1) { // Did the other end close?
            sc.close();
        } else if (bytesRead > 0) {
        	
        	if(!manageClose(key, buf)) {
        		switch (state) {
        		 case AUTH_REQUIRED: case LOGGED_OUT:
        			validateLogin(key, buf);
        			break;
        		case PASS_REQUIRED:
        			validatePassword(key, buf);
        			break;
        		case AUTHORIZED:
        			manageCommands(key, buf);
        			break;
        		}
        	}
        	
        	System.out.println("config_server_read: " + bytesRead + "b");
        }
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        
		buf.flip();
		int nwrite = sc.write(buf);
		
		System.out.println("config_client_write: " + nwrite + "b");
		
		if(!buf.hasRemaining()) {
			buf.clear();
			key.interestOps(SelectionKey.OP_READ);
		}else{
			buf.compact();
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		sc.configureBlocking(false);
		connectionMap.addConnection(sc, sc);
		sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(configLoader.getBufferSize()));
	}

	private void validateLogin(SelectionKey key, ByteBuffer buf) {
		String adminUsername = configLoader.getAdminUsername();
		String content = new String(request.array());
		
		if(content.toUpperCase().startsWith(Commands.USER.toString())) {
			if(content.substring(5, content.length()).trim().equals(adminUsername)) {
				response = "+ OK. Enter password.\n";
				state = HandlerState.PASS_REQUIRED;
			} else {
				state = HandlerState.AUTH_REQUIRED;
				response = "- ERR. Wrong username, please try again.\n";
			}
		} else {
			if(!state.equals(HandlerState.LOGGED_OUT)) {
				response = "- ERR. Must login first.\n";
			} else {
				state = HandlerState.AUTH_REQUIRED;
			}
		}
		
		sendResponse(key, buf);
	}
	
	private void validatePassword(SelectionKey key, ByteBuffer buf) {
		String adminPassword = configLoader.getAdminPassword();
		String content = new String(request.array());
		
		if(content.toUpperCase().startsWith(Commands.PASS.toString())) {
			if(content.substring(5, content.length()).trim().equals(adminPassword)) {
				response = "+ OK. Logged in.\n";
				state = HandlerState.AUTHORIZED;
			} else {
				response = "- ERR. Wrong password, please try again.\n";
			}
		} else if(content.toUpperCase().startsWith(Commands.USER.toString())) {
			state = HandlerState.AUTH_REQUIRED;
			validateLogin(key, buf);
		} else {
			response = "- ERR. Must enter a password.\n";
		}
		
		sendResponse(key, buf);
	}
	
	private void sendResponse(SelectionKey key, ByteBuffer buf) {
		if(response != "") {
			buf.clear();
			buf.put(response.getBytes());
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}
	
	private boolean manageClose(SelectionKey key, ByteBuffer buf) throws IOException {
		String content = new String(request.array());
		
		if(content.toUpperCase().startsWith(Commands.LOGOUT.toString())) {
			state = HandlerState.LOGGED_OUT;
			response = "+ OK. Logged out.\n";
			sendResponse(key, buf);
			return false;
		} else if(content.toUpperCase().startsWith(Commands.QUIT.toString())) {
			state = HandlerState.AUTH_REQUIRED;
			response = "+ OK. Closing connection...\n";
			sendResponse(key, buf);
			SocketChannel sc = (SocketChannel) key.channel();
			sc.close();
			return true;
		}
		return false;
	}
	
	private void manageCommands(SelectionKey key, ByteBuffer buf) {
		String content = new String(request.array());
		
		if(content.toUpperCase().startsWith(Commands.ADDBLACKIP.toString())) {
			String ip = content.substring(10, content.length()).trim();
			if(configUtils.ipIsValid(ip)) {
				configLoader.addBlackIp(ip);
				response = "+ OK. Ip blacklisted.\n";
			} else {
				response = "- ERR. Ip could not be blacklisted.\n";
			}
			sendResponse(key, buf);
			System.out.println(configLoader.getIPBlacklist());
		} else if(content.toUpperCase().startsWith(Commands.REMOVEBLACKIP.toString())) {
			String ip = content.substring(13, content.length()).trim();
			if(configUtils.ipIsValid(ip) && configLoader.removeBlackIp(ip)) {
				response = "+ OK. Ip removed from blacklist.\n";
			} else {
				response = "- ERR. Ip could not be removed.\n";
			}
			sendResponse(key, buf);
			System.out.println(configLoader.getIPBlacklist());
		} 
	}
}
