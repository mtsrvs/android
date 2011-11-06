package ar.edu.itba.it.pdc.proxy.parser.processor;

import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;

public interface XMPPFilter {

	/**
	 * Maneja las stanzas de tipo IQ.
	 * @param iqStanza
	 */
	public void handleIqStanza(IQStanza iqStanza) throws AccessControlException;
	
	/**
	 * Maneja las stanzas de tipo Message.
	 * @param messageStanza
	 */
	public void handleMessageStanza(MessageStanza messageStanza);
	
	/**
	 * Maneja las stanzas de tipo Presence.
	 * @param presenceStanza
	 */
	public void handlePresenceStanza(PresenceStanza presenceStanza);
	
	/**
	 * Maneja todos los demás tipos de elementos.
	 * @param simpleElement
	 * @throws MaxLoginsAllowedException 
	 */
	public void handleOtherElement(SimpleElement simpleElement) throws MaxLoginsAllowedException;
}
