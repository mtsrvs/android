package ar.edu.itba.it.pdc.proxy.protocol;

import java.net.ProtocolException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.IsecuFactory;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.handlers.TCPHandler;

public class ProtocolUtils {

	private int proxyPort;
	private int configPort;
	
	public ProtocolUtils(int proxyPort, int configPort){
		this.proxyPort = proxyPort;
		this.configPort = configPort;
	}
	
	public Protocol expectedProtocol(SelectionKey key) {
		int port;
		
		if(key.channel() instanceof ServerSocketChannel) {
			port = ((ServerSocketChannel) key.channel()).socket().getLocalPort();
		}else if(key.channel() instanceof SocketChannel) {
			port = ((SocketChannel) key.channel()).socket().getLocalPort();
		}else{
			throw new IllegalArgumentException("El key debe tener un SocketChannel o ServerSocketChannel");
		}
		
		if(port == proxyPort) {
			return Protocol.CLIENT;
		}else if(port == configPort) {
			return Protocol.CONFIG;
		}else {
			return Protocol.SERVER;
		}
	}
	
	public TCPHandler getHandler(SelectionKey key) throws ConfigurationFileException {
		IsecuFactory factory = IsecuFactory.getInstance();
		Protocol p = factory.getProtocolUtils().expectedProtocol(key);
		switch(p){
		case CLIENT:
			return factory.getClientHandler();
		case SERVER:
			return factory.getServerHandler();
		case CONFIG:
			return factory.getConfigHandler();
		}
		throw new RuntimeException(new ProtocolException("Invalid protocol"));
	}
	
}
