package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

/**
 * Información que se attachea al socketChannel
 */
public class ChannelAttach {

	private ByteBuffer buffer;
	
	public ChannelAttach(int bufferSize) {
		this.buffer = ByteBuffer.allocate(bufferSize);
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
}
