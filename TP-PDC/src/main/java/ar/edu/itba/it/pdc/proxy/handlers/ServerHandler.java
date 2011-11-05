package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPMessageProcessor;

@Component
public class ServerHandler extends XMPPHandler {

	public void accept(SelectionKey key) throws IOException {
		throw new ProtocolException("It doesn't accept connections");
	}

	@Override
	protected XMPPMessageProcessor getProcessor(SelectionKey key, Opt opt) {
		return opt == Opt.READ ? getAttach(key).getServerProcessor() : getAttach(key).getClientProcessor();
	}

	@Override
	protected ByteBuffer getReadBuffer(SelectionKey key) {
		return getAttach(key).getReadServerBuffer();
	}

	@Override
	protected ByteBuffer getWriteBuffer(SelectionKey key) {
		return getAttach(key).getWriteServerBuffer();
	}

	@Override
	protected String getName() {
		return "Server";
	}

	@Override
	protected void setWriteBuffer(SelectionKey key, ByteBuffer wb) {
		getAttach(key).setWriteServerBuf(wb);
	}

}
