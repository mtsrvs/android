package ar.edu.itba.it.pdc.proxy.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelToolkit {

	private ByteBuffer buffer;
	private SocketChannel endpoint;
	
	public ChannelToolkit(int bufferSize, SocketChannel endpoint){
		this.buffer = ByteBuffer.allocate(bufferSize);
		this.endpoint = endpoint;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public SocketChannel getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(SocketChannel endpoint) {
		this.endpoint = endpoint;
	}
}
