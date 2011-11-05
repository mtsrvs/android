package ar.edu.itba.it.pdc.proxy.parser.element;

public class StartDocumentElement extends XMPPElement {

	private String version;
	private String encoding;
	
	public StartDocumentElement(SimpleElement parent, String version, String encoding) {
		super(parent);
		this.version = version;
		this.encoding = encoding;
	}

	@Override
	protected void appendDataToWrite(StringBuilder builder) {
		builder.append("<?xml version=\"" + this.version + "\"");
		if(this.encoding != null) {
			builder.append(" encoding=\"" + this.encoding + "\"");
		}
		builder.append("?>");
	}
	
	@Override
	public boolean isStartDocumentElement(){
		return true;
	}
	
}
