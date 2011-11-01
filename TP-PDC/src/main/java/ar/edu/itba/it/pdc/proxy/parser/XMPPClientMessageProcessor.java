package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, xmppProcessorMap);
	}

	@Override
	protected void processXMPPElement(SimpleElement e) {
		
	}
	
}
