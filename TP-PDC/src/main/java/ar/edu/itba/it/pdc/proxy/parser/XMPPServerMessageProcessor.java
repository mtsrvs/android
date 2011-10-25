package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.proxy.handlers.ReaderFactory;

public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	public XMPPServerMessageProcessor(ReaderFactory readerFactory) {
		super(readerFactory);
	}

	@Override
	protected void handleStartDocument(int vLocation) {
		sendEvent(vLocation);
	}

	@Override
	protected void handleStartElement(int vLocation) {
		switch(getTagType(getReader().getName().getLocalPart())) {
		case SUCCESS:
			System.out.println("\nLlega success!\n");
			
			sendEvent(vLocation);break;
		default:
			sendEvent(vLocation);
		}
		
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
