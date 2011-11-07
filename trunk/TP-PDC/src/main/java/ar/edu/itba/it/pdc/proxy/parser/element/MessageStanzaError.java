package ar.edu.itba.it.pdc.proxy.parser.element;

import ar.edu.itba.it.pdc.proxy.parser.element.util.PredefinedMessages;

public class MessageStanzaError extends XMPPElement {

	private String to;
	private String message;
	
	public MessageStanzaError(SimpleElement parent, String to, String message) {
		super(parent);
		this.to = to;
		this.message = message;
	}

	@Override
	public void appendDataToWrite(StringBuilder builder) {
		builder.append(PredefinedMessages.createMessageStanzaError("not-acceptable", to, message));
	}
	
}
