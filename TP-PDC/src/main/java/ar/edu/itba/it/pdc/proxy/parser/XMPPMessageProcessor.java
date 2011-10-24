package ar.edu.itba.it.pdc.proxy.parser;

public class XMPPMessageProcessor extends MessageProcessor {

	@Override
	public void handleStartDocument(int vLocation) {
		System.out.println("StartDoc: " + getEventString(vLocation));
		sendEvent(vLocation);
	}

	@Override
	public void handleStartElement(int vLocation) {
		System.out.println("StartElem: " + getEventString(vLocation));
		sendEvent(vLocation);
	}

	@Override
	public void handleAttribute(int vLocation) {
		System.out.println("Attribute: " + getEventString(vLocation));
		sendEvent(vLocation);
	}

	
}
