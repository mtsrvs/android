package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory) {
		super(configLoader, readerFactory);
	}

	@Override
	protected void handleStartElement(int vLocation) {
		switch(getTagType(getReader().getName().getLocalPart())) {
		case SUCCESS:
			System.out.println("Se marca server para resetear");
			this.markToReset();
			sendEvent(vLocation);break;
		default:
			sendEvent(vLocation);
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