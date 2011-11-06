package ar.edu.itba.it.pdc.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.handlers.TCPHandler;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;

@Component
public class ConfigHandler implements TCPHandler {

	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	private ConfigCommandsProcessor commandsProcessor;

	@Autowired
	public ConfigHandler(ConfigLoader configLoader, ConnectionMap connectionMap, 
			ConfigCommandsProcessor commandsProcessor) {
		this.configLoader = configLoader;
		this.connectionMap = connectionMap;
		this.commandsProcessor = commandsProcessor;
	}

	public void read(SelectionKey key, SelectionKey endPointKey)
			throws IOException {

		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		ByteBuffer request = ByteBuffer.allocate(configLoader.getBufferSize());

		request.clear();
		long bytesRead = sc.read(request);
		buf.clear();

		if (bytesRead == -1) { // Did the other end close?
			sc.close();
		} else if (bytesRead > 0) {
			String requestContent = new String(request.array());
			requestContent = requestContent.substring(0, requestContent.indexOf('\n'));

			commandsProcessor.process(key, buf, requestContent);
		}
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();

		buf.flip();
		int nwrite = sc.write(buf);

		if (!buf.hasRemaining()) {
			buf.clear();
			key.interestOps(SelectionKey.OP_READ);
		} else {
			buf.compact();
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		sc.configureBlocking(false);
		connectionMap.addConnection(sc, sc);
		sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer
				.allocate(configLoader.getBufferSize()));
	}

}
