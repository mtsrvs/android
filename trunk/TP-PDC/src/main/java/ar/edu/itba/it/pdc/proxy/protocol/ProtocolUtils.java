package ar.edu.itba.it.pdc.proxy.protocol;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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
	
}
