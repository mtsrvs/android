package ar.edu.itba.it.pdc.proxy.parser.element;

import ar.edu.itba.it.pdc.proxy.parser.element.util.PredefinedMessages;

public class SASLFailure extends XMPPElement {
	
	private String type;
	private String message;
	
	public SASLFailure(SimpleElement parent, String type, String message) {
		super(parent);
		this.type = type;
		this.message = message;
	}

	@Override
	public void appendDataToWrite(StringBuilder builder) {
		builder.append(PredefinedMessages.createSASLFailure(this.type, message));
		builder.append("</stream:stream>");
	}
}
