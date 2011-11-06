package ar.edu.itba.it.pdc.proxy.parser.element;


public abstract class Stanza extends SimpleElement {
	
	public Stanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
	}
	
	@Override
	public boolean isStanza(){
		return true;
	}
	
	public boolean isMessageStanza(){
		return false;
	}
	
	public boolean isIQStanza(){
		return false;
	}
	
	public boolean isPresenceStanza(){
		return false;
	}
	
	public String getType() {
		return this.getAttributes().get("type");
	}
	
}
