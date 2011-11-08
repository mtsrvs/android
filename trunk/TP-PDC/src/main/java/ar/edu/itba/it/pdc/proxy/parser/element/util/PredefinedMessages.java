package ar.edu.itba.it.pdc.proxy.parser.element.util;

import ar.edu.itba.it.pdc.proxy.filetransfer.XMPPFileInfo;
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
		XML.append("<iq type=\"")
		.append(type)
		.append("\" id=\"")
		.append(id);
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
		XML.append("<stream:error>")
		.append("<" + type + "xmlns=\"urn:ietf:params:xml:ns:xmpp-streams\"/>")
		.append("<text xml:lang=\"en\" xmlns=\"urn:ietf:params:xml:ns:xmpp-streams\">")
		.append(message)
		.append("</text>")
		.append("</stream:error>");
		return XML.toString();
	}
	
	public static String createMessageStanzaError(String error, String type, String to, String message){
		StringBuilder XML = new StringBuilder();
		XML.append("<message to=\"" + to + "\" type=\"error\">")
		.append("<body>" + message + "</body>")
		.append("<error type=\"" + type + "\">")
		.append("<" + error + " xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>")
		//.append("<text xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\">")
		//.append(message)
		//.append("</text>")
		.append("</error>")
		.append("</message>");
		return XML.toString();
	}
	
	public static String createSASLFailure(String type, String message) {
		StringBuilder XML = new StringBuilder();
		XML.append("<failure xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">")
		.append("<" + type + ">")
		.append(message)
		.append("</" + type + ">")
		.append("</failure>");
		return XML.toString();
	}
	
	public static XMPPElement createDiscoveryInfo(String from, String to) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader("isecurequest", "get", from, to))
		.append("<query xmlns='http://jabber.org/protocol/disco#info'/>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}
	
	public static XMPPElement createSiOffer(XMPPFileInfo file) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqHeader(file.getId(), "set", file.getFrom(), file.getTo()))
		.append("<si xmlns='http://jabber.org/protocol/si' id='")
		.append(file.getId())
		.append("' profile='http://jabber.org/protocol/si/profile/file-transfer'>")
		.append("<file xmlns='http://jabber.org/protocol/si/profile/file-transfer' name='")
		.append(file.getName())
		.append("' size='").append(file.getSize()).append("'/>")
		.append("<feature xmlns='http://jabber.org/protocol/feature-neg'>")
		.append("<x xmlns='jabber:x:data' type='form'>")
		.append("<field var='stream-method' type='list-single'>")
		.append("<option><value>http://jabber.org/protocol/bytestreams</value></option>")
		.append("<option><value>http://jabber.org/protocol/ibb</value></option>")
		.append("</field>")
		.append("</x>")
		.append("</feature>")
		.append("</si>")
		.append("</iq>");
		return new RawData(null, XML.toString());
	}

	public static String createPresenceUnavailable(String from, String message) {
		StringBuilder XML = new StringBuilder();
		XML.append("<presence from=\"" + from + "\" type=\"unavailable\">");
		XML.append("<status>" + message + "</status>");
		XML.append("</presence>");
		return XML.toString();
	}
	
}
