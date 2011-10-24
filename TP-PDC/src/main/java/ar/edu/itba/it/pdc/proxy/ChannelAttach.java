package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

import ar.edu.itba.it.pdc.proxy.parser.MessageProcessor;

/**
 * Información que se attachea al socketChannel
 */
public class ChannelAttach {

	private ByteBuffer readClientBuf;
	private ByteBuffer writeClientBuf;

	private ByteBuffer serverBuf;
	
	private MessageProcessor processor;
	
	public ChannelAttach(int bufferSize) {
		this.readClientBuf = ByteBuffer.allocate(bufferSize);
		this.writeClientBuf = ByteBuffer.allocate(bufferSize);
		this.serverBuf = ByteBuffer.allocate(bufferSize);
		this.processor = new MessageProcessor();
	}

	public ByteBuffer getReadClientBuffer() {
		return readClientBuf;
	}
	
	public ByteBuffer getWriteClientBuffer() {
		return writeClientBuf;
	}
	
	public ByteBuffer getServerBuffer() {
		return this.serverBuf;
	}
	
	
	public MessageProcessor getProcessor() {
		return this.processor;
	}

}
