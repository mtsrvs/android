package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, xmppProcessorMap);
	}

	private boolean resetMessage = false;
	

	@Override
	protected void processXMPPElement(SimpleElement e) {
		if(e.getName().contains("success")) {
			this.markToReset();
			this.resetMessage = true;
		}
	}

	@Override
	public boolean hasResetMessage() {
		if(this.resetMessage) {
			this.resetMessage = false;
			return true;
		}
		return false;
	}
	
	

}
