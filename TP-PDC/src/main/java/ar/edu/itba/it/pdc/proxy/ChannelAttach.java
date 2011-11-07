package ar.edu.itba.it.pdc.proxy;

import java.nio.ByteBuffer;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPClientMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPServerMessageProcessor;

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
	
	public ChannelAttach(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls, AccessControls accessControls, FileTransferManager fileManager) {
		int bufferSize = configLoader.getBufferSize();
		this.readClientBuf = ByteBuffer.allocate(bufferSize);
		this.readServerBuf = ByteBuffer.allocate(bufferSize);
		
		this.clientProcessor = new XMPPClientMessageProcessor(configLoader, readerFactory, filterControls, accessControls, fileManager);
		this.serverProcessor = new XMPPServerMessageProcessor(configLoader, readerFactory, filterControls, accessControls, fileManager);
		
		this.clientProcessor.setEndpoint(this.serverProcessor);
		this.serverProcessor.setEndpoint(this.clientProcessor);
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
	
	public void setWriteClientBuf(ByteBuffer writeClientBuf) {
		this.writeClientBuf = writeClientBuf;
	}

	public void setWriteServerBuf(ByteBuffer writeServerBuf) {
		this.writeServerBuf = writeServerBuf;
	}

	public XMPPMessageProcessor getClientProcessor() {
		return this.clientProcessor;
	}
	
	public XMPPMessageProcessor getServerProcessor() {
		return this.serverProcessor;
	}

}
