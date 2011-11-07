package ar.edu.itba.it.pdc.proxy.parser.element.util;

import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;

public class PredefinedMessages {

	public static XMPPElement notSupportedFeature(String id, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqErrorHeader(id, from, to));
		XML.append("<error type='cancel'>");
		XML.append("<service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>");
		XML.append("</error>");
		XML.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	private static String createIqErrorHeader(String id, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append("<iq type=\"error\" id=\"");
		XML.append(id);
		XML.append("\" from=\"");
		XML.append(from);
		XML.append("\" to=\"");
		XML.append(to);
		XML.append("\">");
		return XML.toString();
	}
	
}
