package ar.edu.itba.it.pdc.proxy.protocol;

import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.handlers.ClientHandler;
import ar.edu.itba.it.pdc.proxy.handlers.ConfigHandler;
import ar.edu.itba.it.pdc.proxy.handlers.ServerHandler;
import ar.edu.itba.it.pdc.proxy.handlers.TCPHandler;

@Component
public class ProtocolUtils {

	private ConfigLoader configLoader;

	private ClientHandler clientHandler;
	private ServerHandler serverHandler;
	private ConfigHandler configHandler;
	
	@Autowired
	public ProtocolUtils(ConfigLoader configLoader, ClientHandler clientHandler, ServerHandler serverHandler, ConfigHandler configHandler){
		this.configLoader = configLoader;
		this.clientHandler = clientHandler;
		this.serverHandler = serverHandler;
		this.configHandler = configHandler;
	}
	
	public Protocol expectedProtocol(SelectionKey key) {
		int port;
		int proxyPort = ((InetSocketAddress)configLoader.getProxyAddress()).getPort();
		int configPort = ((InetSocketAddress)configLoader.getConfigAddress()).getPort();
		
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
		Protocol p = this.expectedProtocol(key);
		switch(p){
		case CLIENT:
			return clientHandler;
		case SERVER:
			return serverHandler;
		case CONFIG:
			return configHandler;
		}
		throw new RuntimeException(new ProtocolException("Invalid protocol"));
	}
	
}
