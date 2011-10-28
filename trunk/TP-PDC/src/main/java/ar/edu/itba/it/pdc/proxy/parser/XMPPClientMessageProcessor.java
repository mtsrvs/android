package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls) {
		super(configLoader, readerFactory, filterControls);
	}
	
}
