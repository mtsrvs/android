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
	
	public void read(SelectionKey key, SelectionKey endPointKey) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getClientBuffer();
		
		int nread;
		if((nread = sc.read(buf)) == -1) {
			sc.close();
			key.cancel();
			endPointKey.channel().close();
			endPointKey.cancel();
		}
		
		System.out.println("client_read: " + nread + "b");

		if(nread > 0) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}else{
			endPointKey.interestOps(SelectionKey.OP_WRITE);
		}
		
	}

	public void write(SelectionKey key) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getServerBuffer();
		
		buf.flip();
		int nwrite = sc.write(buf);
		
		System.out.println("client_write: " + nwrite + "b");
		
		if(!buf.hasRemaining()) {
			key.interestOps(SelectionKey.OP_READ);
		}
		
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel ss;
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		//TODO: Acá debería hacerse la multiplexación de usuarios
		ss = SocketChannel.open(configLoader.getOriginServer());
		ss.configureBlocking(false);
		connectionMap.addConnection(sc, ss);
		sc.configureBlocking(false);
		ChannelAttach attach = new ChannelAttach(configLoader.getBufferSize());
		sc.register(key.selector(), SelectionKey.OP_READ, attach);
		ss.register(key.selector(), SelectionKey.OP_READ, attach);
	}

}
