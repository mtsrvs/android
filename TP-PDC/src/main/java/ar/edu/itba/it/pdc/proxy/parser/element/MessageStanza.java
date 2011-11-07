package ar.edu.itba.it.pdc.proxy.parser.element;

public class MessageStanza extends Stanza {

	public MessageStanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
	}
	
	public String getTo(){
		return this.selement.getAttributes().get("to");
	}
	
	public String getFrom(){
		return this.selement.getAttributes().get("from");
	}
	
	@Override
	public boolean isMessageStanza(){
		return true;
	}

}
