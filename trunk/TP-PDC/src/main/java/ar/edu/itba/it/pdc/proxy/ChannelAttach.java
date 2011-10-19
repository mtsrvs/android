package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

/**
 * Información que se attachea al socketChannel
 */
public class ChannelAttach {

	private ByteBuffer lc;
	private ByteBuffer ls;
	
	public ChannelAttach(int bufferSize) {
		this.lc = ByteBuffer.allocate(bufferSize);
		this.ls = ByteBuffer.allocate(bufferSize);
	}

	public ByteBuffer getClientBuffer() {
		return lc;
	}
	
	public ByteBuffer getServerBuffer() {
		return ls;
	}
	
}
