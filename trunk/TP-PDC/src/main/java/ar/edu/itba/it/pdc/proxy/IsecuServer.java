package ar.edu.itba.it.pdc.proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import ar.edu.itba.it.pdc.IsecuFactory;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.info.ProxyInfo;
import ar.edu.itba.it.pdc.proxy.protocol.Protocol;
import ar.edu.itba.it.pdc.proxy.protocol.ProtocolUtils;

public class IsecuServer {
	
	public static final int BUFFER_SIZE = 512;
	
	public static final int READ = SelectionKey.OP_READ;
	public static final int WRITE = SelectionKey.OP_WRITE;
	public static final int ACCEPT = SelectionKey.OP_ACCEPT;
	
	private IsecuFactory factory;
	private ProxyInfo proxyInfo;
	
	/**
	 * Inicializa el proxy
	 * @param origin Address del servidor origin default.
	 * @param originPort Puerto del servidor origin default.
	 * @param proxy Interfaz donde se bindea el servidor proxy.
	 * @param proxyPort Puerto donde se bindea el servidor proxy.
	 * @throws ConfigurationFileException 
	 */
	public IsecuServer() throws ConfigurationFileException{
		this.factory = IsecuFactory.getInstance();
		this.proxyInfo = factory.getConfigLoader().getProxyInfo();
	}
	
	
	/**
	 * Inicia el servidor proxy
	 * @throws IOException
	 */
	public void start() throws IOException {
		
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(proxyInfo.getProxy());
		serverChannel.register(selector, ACCEPT);
	
		ServerSocketChannel configChannel = ServerSocketChannel.open();
		configChannel.configureBlocking(false);
		configChannel.socket().bind(proxyInfo.getConfig());
		configChannel.register(selector, ACCEPT);
		
		System.out.println("Servidor isecu inicializado en: " + proxyInfo.getProxy());
		
		while(true){
			
			selector.select();
			
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                if (key.isValid()){
                	
	                if (key.isAcceptable()) {
	                	this.handleAccept(key);
	                }
	                
	                if (key.isReadable()) {
	                	this.handleRead(key);
	                }
	                
	                if (key.isWritable()) {
	                	this.handleWrite(key);
	                }
	                
	                iterator.remove();
                }
            }
		}
	}
	
	private void handleWrite(SelectionKey key) {
		try {
			factory.getProtocolUtils().getHandler(key).write(key);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Handle write error");
		}
	}
	
	private void handleRead(SelectionKey key) {
		ProtocolUtils pu = factory.getProtocolUtils();
		Protocol p = pu.expectedProtocol(key);
		SocketChannel endPoint = null;
		if(p == Protocol.CLIENT || p == Protocol.SERVER) {
			endPoint = factory.getConnectionMap().getServerChannel(key.channel());
		}
		try {
			factory.getProtocolUtils().getHandler(key).read(key, endPoint);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Handler read error");
		}
	}
	
	/**
	 * Acepta conexiones 
	 * @param key
	 */
	private void handleAccept(SelectionKey key) {
		try {
			factory.getProtocolUtils().getHandler(key).accept(key);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Handle accept error");
		}
	}
	
	
}
