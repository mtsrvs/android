package ar.edu.itba.it.pdc.proxy.parser.element;

public class AccessControlFailure extends XMPPElement {

	private final static String NS_STREAMS = "urn:ietf:params:xml:ns:xmpp-streams";
	private final static String LANG_EN = "en";
	
	private String message;
	
	public AccessControlFailure(SimpleElement parent, String message) {
		super(parent);
		this.message = message;
	}

	@Override
	protected void appendDataToWrite(StringBuilder builder) {
		builder.append("<stream:error>");
		builder.append("<invalid-from xmlns=\"" + NS_STREAMS + "\"/>");
		builder.append("<text xml:lang=\"" + LANG_EN + "\" xmlns=\"" + NS_STREAMS + "\">");
		builder.append(message);
		builder.append("</text>");
		builder.append("</stream:error>");
		builder.append("</stream:stream>");
	}
}
