package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.proxy.handlers.ReaderFactory;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ReaderFactory readerFactory) {
		super(readerFactory);
	}
	
	@Override
	public void handleStartDocument(int vLocation) {
		System.out.println("Start document client");
		sendEvent(vLocation);
	}

	@Override
	public void handleStartElement(int vLocation) {
		System.out.println("Start element client: " + getReader().getName().getLocalPart());
		sendEvent(vLocation);
	}

	@Override
	public void handleAttribute(int vLocation) {
		System.out.println("Attribute client");
		sendEvent(vLocation);
	}

	@Override
	protected void handleEndElement(int vLocation) {
		System.out.println("End element client");
		sendEvent(vLocation);
	}
	
	@Override
	protected void handleAnyOtherEvent(int vLocation) {
		sendEvent(vLocation);
	}
	
}
