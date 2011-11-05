package ar.edu.itba.it.pdc.proxy.parser.element;

public class IQStanza extends Stanza {

	public IQStanza(SimpleElement parent, StartElement selement) {
		super(parent, selement);
	}
	
	@Override
	public boolean isIQStanza(){
		return true;
	}

}
