package ar.edu.itba.it.pdc.proxy.parser.element;

import ar.edu.itba.it.pdc.proxy.parser.element.util.PredefinedMessages;

public class PresenceUnavailable extends XMPPElement {

	private String jid;
	private String message;
	
	public PresenceUnavailable(SimpleElement parent, String jid, String message) {
		super(parent);
		this.jid = jid;
		this.message = message;
	}

	@Override
	public void appendDataToWrite(StringBuilder builder) {
		builder.append(PredefinedMessages.createPresenceUnavailable(jid, message));
	}
}
