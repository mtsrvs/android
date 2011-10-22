package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.ChannelAttach;

@Component
public class ServerHandler implements TCPHandler {

	public void read(SelectionKey key, SelectionKey endPointKey) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getServerBuffer();

		int nread;
		if((nread = sc.read(buf)) == -1) {
			sc.close();
			key.cancel();
			endPointKey.channel().close();
			endPointKey.cancel();
			return;
		}
		
		System.out.println("server_read: " + nread + "b");
		
		
		if(nread > 0) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}else{
			endPointKey.interestOps(SelectionKey.OP_WRITE);
		}
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		ByteBuffer buf = attach.getClientBuffer();
		
		buf.flip();
		int nwrite = sc.write(buf);
		
		System.out.println("server_write: " + nwrite + "b");

		if(!buf.hasRemaining()) {
			buf.clear();
			key.interestOps(SelectionKey.OP_READ);
		}else{
			buf.compact();
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		
	}

	public void accept(SelectionKey key) throws IOException {
		throw new ProtocolException("It doesn't accept connections");
	}

}
