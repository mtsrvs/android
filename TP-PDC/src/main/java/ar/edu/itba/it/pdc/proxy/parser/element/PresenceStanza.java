package ar.edu.itba.it.pdc.proxy.parser.element;

public class PresenceStanza extends Stanza {

	public PresenceStanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
	}
	
	@Override
	public boolean isPresenceStanza(){
		return true;
	}

}
