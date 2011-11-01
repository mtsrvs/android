package ar.edu.itba.it.pdc.proxy.parser.element;


public class Stanza extends SimpleElement {

	protected static enum StanzaType {
		MESSAGE, IQ, PRESENCE
	}
	
	protected StanzaType stanzaType;
	
	public Stanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
		this.stanzaType = StanzaType.valueOf(this.selement.getName().toUpperCase());
	}
	
}
