package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.proxy.ChannelAttach;

public class ServerHandler implements TCPHandler {

	public void read(SelectionKey key, SocketChannel sEndPoint) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getBuffer();
		buf.clear();

		int read = sc.read(buf);
		
		if(read == -1) {
			sc.close();
			sEndPoint.close();
			key.cancel();
		}
		
		System.out.println("-> " + buf.array().length + "b");
		
		buf.flip();
		sEndPoint.write(buf);
		
	}

	public void write(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void accept(SelectionKey key) throws IOException {
		throw new ProtocolException("It doesn't accept connections");
	}

}
