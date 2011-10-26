package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory) {
		super(configLoader, readerFactory);
	}
	
}
