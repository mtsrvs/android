package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.XMPPClientMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.XMPPMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.XMPPServerMessageProcessor;

/**
 * Información que se attachea al socketChannel
 */
public class ChannelAttach {

	private ByteBuffer readClientBuf;
	private ByteBuffer writeClientBuf;

	private ByteBuffer readServerBuf;
	private ByteBuffer writeServerBuf;
	
	private XMPPClientMessageProcessor clientProcessor;
	private XMPPServerMessageProcessor serverProcessor;
	
	public ChannelAttach(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls) {
		int bufferSize = configLoader.getBufferSize();
		this.readClientBuf = ByteBuffer.allocate(bufferSize);
		this.writeClientBuf = ByteBuffer.allocate(bufferSize);
		this.readServerBuf = ByteBuffer.allocate(bufferSize);
		this.writeServerBuf = ByteBuffer.allocate(bufferSize);
		
		this.clientProcessor = new XMPPClientMessageProcessor(configLoader, readerFactory, filterControls);
		this.serverProcessor = new XMPPServerMessageProcessor(configLoader, readerFactory, filterControls);
	}

	public ByteBuffer getReadClientBuffer() {
		return readClientBuf;
	}
	
	public ByteBuffer getWriteClientBuffer() {
		return writeClientBuf;
	}
	
	public ByteBuffer getReadServerBuffer() {
		return this.readServerBuf;
	}
	
	public ByteBuffer getWriteServerBuffer() {
		return this.writeServerBuf;
	}
	
	
	public XMPPMessageProcessor getClientProcessor() {
		return this.clientProcessor;
	}
	
	public XMPPMessageProcessor getServerProcessor() {
		return this.serverProcessor;
	}

}
