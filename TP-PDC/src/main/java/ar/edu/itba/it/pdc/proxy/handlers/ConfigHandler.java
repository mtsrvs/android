package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.IsecuServer;

@Component("ConfigHandler")
public class ConfigHandler implements TCPHandler {

	public void read(SelectionKey key, SocketChannel endPoint)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void write(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		sc.configureBlocking(false);
		sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(IsecuServer.BUFFER_SIZE));
	}

}
