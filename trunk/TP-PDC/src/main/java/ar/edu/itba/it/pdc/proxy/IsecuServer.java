package ar.edu.itba.it.pdc.proxy;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.handlers.HandlerUtils;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.protocol.Protocol;
import ar.edu.itba.it.pdc.proxy.protocol.ProtocolUtils;

@Component
public class IsecuServer {

	public static final int READ = SelectionKey.OP_READ;
	public static final int WRITE = SelectionKey.OP_WRITE;
	public static final int ACCEPT = SelectionKey.OP_ACCEPT;

	private ConfigLoader configLoader;
	private ProtocolUtils protocolUtils;
	private ConnectionMap connectionMap;
	
	@Autowired
	public IsecuServer(ConfigLoader configLoader, ProtocolUtils protocolUtils,
			ConnectionMap connectionMap) {
		this.configLoader = configLoader;
		this.protocolUtils = protocolUtils;
		this.connectionMap = connectionMap;		
	}

	/**
	 * Inicia el servidor proxy
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {

		Selector selector = Selector.open();
		
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(configLoader.getProxyAddress());
		serverChannel.register(selector, ACCEPT);

		ServerSocketChannel configChannel = ServerSocketChannel.open();
		configChannel.configureBlocking(false);
		configChannel.socket().bind(configLoader.getConfigAddress());
		configChannel.register(selector, ACCEPT);

		Isecu.log.info("Isecu service initialized at: " + configLoader.getProxyAddress());
		Isecu.log.info("Isecu configuration service initilized at: " + configLoader.getConfigAddress());
		
		System.out.println("Services were initiated.\t[OK]");
		
		while (true) {
			selector.select();

			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				try{
					if (key.isValid()) {
						
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
				} catch(CancelledKeyException e) {
					Isecu.log.debug(e);
				}
			}
		}
	}

	/**
	 * Obtiene el manejador de escritura correspondiente al socket y lo ejecuta.
	 * @param key
	 */
	private void handleWrite(SelectionKey key) {
		try {
			protocolUtils.getHandler(key).write(key);
		} catch (Exception e) {
			Isecu.log.debug("Write handler error", e);
			Isecu.log.error("Write handler error");
		}
	}

	/**
	 * Obtiene el manejador de lectura correspondiente al socket y lo ejecuta.
	 * @param key
	 */
	private void handleRead(SelectionKey key) {
		Protocol p = protocolUtils.expectedProtocol(key);
		SocketChannel endPoint = null;
		if (p == Protocol.CLIENT) {
			endPoint = connectionMap.getServerChannel(key.channel());
		} else if (p == Protocol.SERVER || p == Protocol.CONFIG) {
			endPoint = connectionMap.getClientChannel(key.channel());
		}
		try {
			protocolUtils.getHandler(key).read(key,
					HandlerUtils.getKey(endPoint, key.selector()));
		} catch (Exception e) {
			Isecu.log.debug("Read handler error", e);
			Isecu.log.error("Read handler error");
		}
	}

	/**
	 * Obtiene el manejador de accept correspondiente al socket y lo ejecuta.
	 * 
	 * @param key
	 */
	private void handleAccept(SelectionKey key) {
		try {
			protocolUtils.getHandler(key).accept(key);
		} catch (Exception e) {
			Isecu.log.debug("Accept handler error", e);
			Isecu.log.error("Accept handler error");
		}
	}

}
