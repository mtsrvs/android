package ar.edu.itba.it.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import ar.edu.itba.it.pdc.proxy.handlers.ProxyService;
import ar.edu.itba.it.pdc.proxy.handlers.SelectionHandler;

public class XMPPProxy {
	
	private static final int ACCEPT = SelectionKey.OP_ACCEPT;
	private static final int BUFFER_SIZE = 512;
	private static final int TIMEOUT = 2000;
	
	//TODO ORIGIN SERVER CABLEADO
	private static final String DEFAULT_ORIGIN_SERVER = "localhost";
	private static final int DEFAULT_ORIGIN_PORT = 5222;
	
	private int port;
	private int selectors;
	private SelectionHandler service;
	
	public XMPPProxy(int port, int selectors){
		this.port = port;
		this.selectors = selectors;
		this.service = new ProxyService(DEFAULT_ORIGIN_SERVER, DEFAULT_ORIGIN_PORT, BUFFER_SIZE);
	}
	
	
	// Por ahora con un solo selector
	/*
	 * Si expandimos esto para N selectores (1 por core)
	 * habría que hacer que este método start cree N instancias de
	 * una subclase y las ejecute cada una en un thread
	 * distinto. El método principal de esa subclase sería
	 * muy parecido a este, donde se crea el selector y el canal
	 * de escucha y se le asigna el puerto.
	 */
	public void start() throws IOException {
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		serverChannel.register(selector, ACCEPT);
	
		while(true){
			if (selector.select(TIMEOUT) == 0){
                System.out.print(".");
                continue;
            }
			
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                if (key.isValid()){
                	
	                // Hay solicitudes de conexión?
	                if (key.isAcceptable())
	                    this.service.accept(key);
	                
	                // Data de lectura pendiente?
	                if (key.isReadable())
	                	this.service.read(key);
	                
	                // Data de escritura pendiente?
	                if (key.isWritable()){
	                	//System.out.println("WRITE");
	                	this.service.write(key);
	                }
	                
	                iterator.remove();
                }
            }
		}
	}
}
