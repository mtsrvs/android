package ar.edu.itba.it.pdc.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.naming.ConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.CommandValidationException;
import ar.edu.itba.it.pdc.proxy.handlers.TCPHandler;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;

@Component
public class ConfigHandler implements TCPHandler {

	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	private ConfigCommandsProcessor commandsProcessor;
	private StringBuilder buffer;

	@Autowired
	public ConfigHandler(ConfigLoader configLoader, ConnectionMap connectionMap, 
			ConfigCommandsProcessor commandsProcessor) {
		this.configLoader = configLoader;
		this.connectionMap = connectionMap;
		this.commandsProcessor = commandsProcessor;
		this.buffer = new StringBuilder();
	}

	public void read(SelectionKey key, SelectionKey endPointKey)
			throws IOException {

		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		ByteBuffer request = ByteBuffer.allocate(configLoader.getBufferSize());

		request.clear();
		long bytesRead = sc.read(request);
		buf.clear();

		try {
			if (bytesRead == -1) { // Did the other end close?
				sc.close();
			} else if (bytesRead > 0) {
				String requestContent = new String(request.array());
				checkCtrlC(request.array());
				if(requestContent.contains("\n")) {
					requestContent = requestContent.substring(0, requestContent.indexOf('\n'));
					buffer.append(requestContent);
					commandsProcessor.process(key, buf, buffer.toString());
					buffer.delete(0, buffer.length());
				} else {
					buffer.append(requestContent);
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		} catch (Exception e) {
			sc.close();
		}
	}

	private void checkCtrlC(byte[] array) {
		if(array[0] == -1 && array[1] == -12 && array[2] == -1
				&& array[3] == -3 && array[4] == 6 && array[5] == 0 
				&& array[6] == 0 && array[7] == 0 && array[8] == 0 && array[9] == 0) {
			throw new CommandValidationException();
		}
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();

		buf.flip();
		sc.write(buf);

		if(commandsProcessor.hasRemaining()) {
			commandsProcessor.getContent(buf);
			key.interestOps(SelectionKey.OP_WRITE);
		} else if (!buf.hasRemaining()) {
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
