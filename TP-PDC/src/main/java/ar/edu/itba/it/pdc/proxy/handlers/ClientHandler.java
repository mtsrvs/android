package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.XMPPMessageProcessor;

/**
 * Manejador de los eventos del socket a cliente.
 */
@Component
public class ClientHandler extends XMPPHandler {
	
	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	private ReaderFactory readerFactory;
	
	
	@Autowired
	public ClientHandler(ConfigLoader configLoader, ConnectionMap connectionMap, ReaderFactory readerFactory) {
		this.configLoader = configLoader;
		this.connectionMap = connectionMap;
		this.readerFactory = readerFactory;
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel ss;
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		
		//TODO: Acá debería hacerse la multiplexación de usuarios
		
		ss = SocketChannel.open(configLoader.getOriginServer());
		ss.configureBlocking(false);
		connectionMap.addConnection(sc, ss);
		sc.configureBlocking(false);
		ChannelAttach attach = new ChannelAttach(configLoader.getBufferSize(), this.readerFactory);
		sc.register(key.selector(), SelectionKey.OP_READ, attach);
		ss.register(key.selector(), SelectionKey.OP_READ, attach);
	}

	@Override
	protected XMPPMessageProcessor getProcessor(SelectionKey key, Opt opt) {
		return opt == Opt.READ ? getAttach(key).getClientProcessor() : getAttach(key).getServerProcessor();
 	}

	@Override
	protected ByteBuffer getReadBuffer(SelectionKey key) {
		return getAttach(key).getReadClientBuffer();
	}

	@Override
	protected ByteBuffer getWriteBuffer(SelectionKey key) {
		return getAttach(key).getWriteClientBuffer();
	}

	@Override
	protected String getName() {
		return "Client";
	}


}
