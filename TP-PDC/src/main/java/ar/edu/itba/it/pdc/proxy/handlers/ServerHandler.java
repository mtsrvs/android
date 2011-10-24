package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.parser.MessageProcessor;

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
		
		if(nread > 0) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}else{
			endPointKey.interestOps(SelectionKey.OP_WRITE);
		}
		
	}

	public void write(SelectionKey key) throws IOException {

		System.out.println("Escribe");
		
		SocketChannel sc = (SocketChannel) key.channel();
		ChannelAttach attach = (ChannelAttach) key.attachment();
		MessageProcessor processor = attach.getProcessor();
		ByteBuffer buf = attach.getWriteClientBuffer();
		
		processor.write(buf);
		
		sc.write(buf);
		
		if(!buf.hasRemaining()) {
			buf.clear();
			key.interestOps(SelectionKey.OP_READ);
		}else{
			buf.compact();
		}
		
	}

	public void accept(SelectionKey key) throws IOException {
		throw new ProtocolException("It doesn't accept connections");
	}

}
