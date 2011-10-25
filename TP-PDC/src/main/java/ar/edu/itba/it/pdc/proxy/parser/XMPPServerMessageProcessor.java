package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.proxy.handlers.ReaderFactory;

public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	
	public XMPPServerMessageProcessor(ReaderFactory readerFactory) {
		super(readerFactory);
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
