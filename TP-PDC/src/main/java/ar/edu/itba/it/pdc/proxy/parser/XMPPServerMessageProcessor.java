package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls) {
		super(configLoader, readerFactory, filterControls);
	}

	@Override
	protected void handleStartElement(int vLocation) {
		switch(getTagType(getReader().getName().getLocalPart())) {
		case SUCCESS:
			this.markToReset();
			this.messageBuffer.markEventToSend(vLocation);break;
		default:
			this.messageBuffer.markEventToSend(vLocation);
		}
	}

	@Override
	public boolean hasResetMessage() {
		if(resetMessage) {
			this.resetMessage = false;
			return true;
		}
		return false;
	}

	@Override
	public void markToReset() {
		super.markToReset();
		this.resetMessage = true;
	}

}
