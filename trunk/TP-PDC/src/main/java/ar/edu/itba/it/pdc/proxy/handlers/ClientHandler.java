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

/**
 * Manejador de los eventos del socket a cliente.
 */
@Component
public class ClientHandler implements TCPHandler {
	
	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	
	@Autowired
	public ClientHandler(ConfigLoader configLoader, ConnectionMap connectionMap) {
		this.configLoader = configLoader;
		this.connectionMap = connectionMap;
	}
	
	public void read(SelectionKey key, SocketChannel endPoint) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getBuffer();
		buf.clear();
		
		int read = sc.read(buf);
		
		if(read == -1) {
			sc.close();
			endPoint.close();
			key.cancel();
		}
		
		System.out.println("<- " + buf.array().length + "b");
		
		/* Esto está mal, deberíamos escribir en un buffer y marcar como interestOp el write */
		if(key.isValid()) {
			buf.flip();
			endPoint.write(buf);
		}
	}

	public void write(SelectionKey key) {
		
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel ss;
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		//TODO: Acá debería hacerse la multiplexación de usuarios
		ss = SocketChannel.open(configLoader.getOriginServer());
		ss.configureBlocking(false);
		connectionMap.addConnection(sc, ss);
		sc.configureBlocking(false);
		sc.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(configLoader.getBufferSize()));
		ss.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(configLoader.getBufferSize()));
	}

}
