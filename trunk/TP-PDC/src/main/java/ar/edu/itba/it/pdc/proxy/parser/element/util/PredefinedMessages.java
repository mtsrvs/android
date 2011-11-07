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
	
	public static String createStreamError(String type, String message) {
		StringBuilder XML = new StringBuilder();
		XML.append("<stream:error>");
		XML.append("<" + type + "xmlns=\"urn:ietf:params:xml:ns:xmpp-streams\"/>");
		XML.append("<text xml:lang=\"en\" xmlns=\"urn:ietf:params:xml:ns:xmpp-streams\">");
		XML.append(message);
		XML.append("</text>");
		XML.append("</stream:error>");
		return XML.toString();
	}
	
	public static String createMessageStanzaError(String type, String to, String message){
		StringBuilder XML = new StringBuilder();
		XML.append("<message to=\"" + to + "\" type=\"error\">");
		XML.append("<body>" + message + "</body>");
		XML.append("<error type=\"modify\">");
		XML.append("<" + type + " xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>");
		XML.append("<text xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\">");
		XML.append(message);
		XML.append("</text>");
		XML.append("</error>");
		XML.append("</message>");
		return XML.toString();
	}
	
	public static String createSASLFailure(String type, String message) {
		StringBuilder XML = new StringBuilder();
		XML.append("<failure xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
		XML.append("<" + type + ">");
		XML.append(message);
		XML.append("</" + type + ">");
		XML.append("</failure>");
		return XML.toString();
	}
	
}
