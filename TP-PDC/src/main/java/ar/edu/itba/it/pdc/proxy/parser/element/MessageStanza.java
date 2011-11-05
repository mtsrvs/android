package ar.edu.itba.it.pdc.proxy.parser.element;

public class MessageStanza extends Stanza {

	public MessageStanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
	}
	
	@Override
	public boolean isMessageStanza(){
		return true;
	}

}
