package ar.edu.itba.it.pdc.proxy.parser.element.util;

import java.util.List;

import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;

public class PredefinedMessages {

	public static XMPPElement notSupportedStreamMethods(String from, String to, String id, List<String> methods) {
		StringBuilder XML = new StringBuilder();
		XML.append(createIqErrorHeader(id, from, to));
		XML.append("<feature xmlns='http://jabber.org/protocol/feature-neg'>");
		XML.append("<x xmlns='jabber:x:data' type='form'>");
		XML.append("<field var='stream-method' type='list-single'>");
		for(String m : methods) {
			XML.append("<option><value>");
			XML.append(m);
			XML.append("</value></option>");
		}
		XML.append("</field>");
		XML.append("</x>");
		XML.append("</feature>");
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
