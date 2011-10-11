package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

public class ChannelAttach {

	private ByteBuffer buffer;
	
	public ChannelAttach(int bufferSize) {
		this.buffer = ByteBuffer.allocate(bufferSize);
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
}
