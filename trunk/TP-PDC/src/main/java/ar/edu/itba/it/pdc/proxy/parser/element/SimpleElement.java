package ar.edu.itba.it.pdc.proxy.parser.element;

import java.util.LinkedList;
import java.util.List;

import ar.edu.itba.it.pdc.exception.InvalidProtocolException;

public class SimpleElement extends XMPPElement {
	
	protected StartElement selement;
	private List<XMPPElement> body = new LinkedList<XMPPElement>();
	
	public SimpleElement(SimpleElement parent, StartElement selement) {
		super(parent);
		this.selement = selement;
	}

	@Override
	protected void appendDataToWrite(StringBuilder builder) {
		this.selement.appendDataToWrite(builder);
		for(XMPPElement e : this.body) {
			e.appendDataToWrite(builder);
		}
		builder.append("</" + this.selement.getName() + ">");
	}
	
	public void appendBody(XMPPElement e) {
		this.body.add(e);
	}
	
	public void appendEndElement(String name) {
		if(!this.selement.getName().equals(name)) {
			throw new InvalidProtocolException("Malformed stream");
		}
	}
	
	public String getName() {
		return this.selement.getName();
	}
	
	public StartElement getStartElement(){
		return this.selement;
	}
	
	public List<XMPPElement> getBody(){
		return this.body;
	}
	
	@Override
	public boolean isSimpleElement(){
		return true;
	}
	
	public boolean isStanza(){
		return false;
	}

}
