package ar.edu.itba.it.pdc.proxy.parser.element.util;

import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;

public class PredefinedMessages {

	public static XMPPElement notSupportedFeature(String id, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(id, "error", from, to))
		.append("<error type='cancel'>")
		.append("<service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>")
		.append("</error>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	public static XMPPElement queryDiscoInfo(String id, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(id, "get", from, to))
		.append("<query xmlns='http://jabber.org/protocol/disco#info'/>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	public static XMPPElement siFileTransferResult(String id, String to, String from, String streamMethod) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(id, "result", from, to))
		.append("<si xmlns='http://jabber.org/protocol/si'>")
		.append("<feature xmlns='http://jabber.org/protocol/feature-neg'>")
		.append("<x xmlns='jabber:x:data' type='submit'>")
		.append("<field var='stream-method'>")
		.append("<value>")
		.append(streamMethod)
		.append("</value>")
		.append("</field>")
		.append("</x>")
		.append("</feature>")
		.append("<file xmlns=\"http://jabber.org/protocol/si/profile/file-transfer\"/>")
		.append("</si>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	public static XMPPElement streamHostUsed(String id, String from, String to, String jid) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(id, "result", from, to))
		.append("<query xmlns=\"http://jabber.org/protocol/bytestreams\">")
		.append("<streamhost-used jid=\"")
		.append(jid)
		.append("\"/>")
		.append("</query>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	public static XMPPElement streamHostFail(String id, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(id, "error", from, to))
		.append("<error type='cancel'><item-not-found xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/></error>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	private static String createIqHeader(String id, String type, String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append("<iq type=\"");
		XML.append(type);
		XML.append("\" id=\"");
		XML.append(id);
		if(from != null) {
			XML.append("\" from=\"");
			XML.append(from);
		}
		if(to != null) {
			XML.append("\" to=\"");
			XML.append(to);
		}
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
