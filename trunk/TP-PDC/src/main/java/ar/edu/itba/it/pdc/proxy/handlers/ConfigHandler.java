package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;

@Component
public class ConfigHandler implements TCPHandler {

	private ConfigLoader configLoader;
	
	@Autowired
	public ConfigHandler(ConfigLoader configLoader) {
		this.configLoader = configLoader;
	}
	
	public void read(SelectionKey key, SelectionKey ekey)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void write(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		sc.configureBlocking(false);
		sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(configLoader.getBufferSize()));
	}

}
