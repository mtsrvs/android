package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.IsecuFactory;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.IsecuServer;
import ar.edu.itba.it.pdc.proxy.info.ProxyInfo;

/**
 * Manejador de los eventos del socket a cliente.
 */
public class ClientHandler implements TCPHandler {
	
	private IsecuFactory factory = IsecuFactory.getInstance();
	
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
		buf.flip();
		endPoint.write(buf);
	}

	public void write(SelectionKey key) {
		
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel ss;
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		ProxyInfo proxyInfo = factory.getConfigLoader().getProxyInfo();
		//Acá debería hacerse la multiplexación de usuarios
		ss = SocketChannel.open(proxyInfo.getOrigin());
		ss.configureBlocking(false);
		factory.getConnectionMap().addConnection(sc, ss);
		sc.configureBlocking(false);
		sc.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(IsecuServer.BUFFER_SIZE));
		ss.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(IsecuServer.BUFFER_SIZE));
	}

}
