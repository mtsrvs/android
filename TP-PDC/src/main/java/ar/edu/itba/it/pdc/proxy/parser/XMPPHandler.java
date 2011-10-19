package ar.edu.itba.it.pdc.proxy.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMPPHandler extends DefaultHandler {

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(qName.equalsIgnoreCase("message")) {
			System.out.println("Comienza mensaje");
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(qName.equalsIgnoreCase("message")) {
			System.out.println("Termina mensaje");
		}
	}
	
}
