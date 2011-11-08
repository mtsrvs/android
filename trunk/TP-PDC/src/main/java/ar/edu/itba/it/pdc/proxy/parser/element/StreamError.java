package ar.edu.itba.it.pdc.proxy.parser.element;

import ar.edu.itba.it.pdc.proxy.parser.element.util.PredefinedMessages;

public class StreamError extends XMPPElement {

	private String type;
	private String message;
	
	public StreamError(SimpleElement parent, String type, String message) {
		super(parent);
		this.type = type;
		this.message = message;
	}

	@Override
	public void appendDataToWrite(StringBuilder builder) {
		builder.append(PredefinedMessages.createStreamError(type, message));
		builder.append("</stream:stream>");
	}
	
	public String toString(){
		StringBuilder b = new StringBuilder();
		b.append(PredefinedMessages.createStreamError(type, message));
		return b.toString();
	}
}
