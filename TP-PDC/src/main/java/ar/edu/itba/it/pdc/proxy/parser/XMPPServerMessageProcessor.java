package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.proxy.handlers.ReaderFactory;

public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	public XMPPServerMessageProcessor(ReaderFactory readerFactory) {
		super(readerFactory);
	}

	@Override
	protected void handleStartDocument(int vLocation) {
		System.out.println("Start document server");
		sendEvent(vLocation);
	}

	@Override
	protected void handleStartElement(int vLocation) {
		System.out.println("Start element server: " + getReader().getName().getLocalPart());
		sendEvent(vLocation);
	}

	@Override
	protected void handleAttribute(int vLocation) {
		System.out.println("Attribute server");
		sendEvent(vLocation);
	}

	@Override
	protected void handleEndElement(int vLocation) {
		System.out.println("End element server");
		sendEvent(vLocation);
	}

	@Override
	protected void handleAnyOtherEvent(int vLocation) {
		System.out.println("Otro evento");
		sendEvent(vLocation);
	}

}
