package ar.edu.itba.it.pdc.proxy.parser.element.util;

import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;

public class ElemUtils {

	public static enum StanzaType {
		MESSAGE, IQ, PRESENCE;
	}
	
	/**
	 * Dado un SimpleElement devuelve si es o no una stanza xmpp.
	 * @param e
	 * @return true/false
	 */
	public static boolean isStanza(SimpleElement e) {
		String name = e.getName();
		for(StanzaType st : StanzaType.values()) {
			if(name.equalsIgnoreCase(st.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indica si un simple element es una stanza de cierto tipo
	 * @param e
	 * @param st
	 * @return
	 */
	public static boolean isStanzaType(SimpleElement e, StanzaType st) {
		return isElement(e, st.toString());
	}
	
	/**
	 * Indica si un elemento posee el nombre especificado.
	 * @param se
	 * @param name
	 * @return
	 */
	public static boolean isElement(SimpleElement se, String name) {
		return se.getLocalName().equalsIgnoreCase(name);
	}

	/**
	 * Comparación de strings
	 * @param text
	 * @return
	 */
	public static boolean hasTextEquals(String text1, String text2) {
		return text1 != null && text1.equalsIgnoreCase(text2);
	}
}
