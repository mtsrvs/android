package ar.edu.itba.it.pdc.proxy.parser.element;


public class RawData extends XMPPElement {

	private String data;
	
	public RawData(SimpleElement parent, String data) {
		super(parent);
		this.data = data;
	}
	
	public String getData(){
		return this.data;
	}

	@Override
	protected void appendDataToWrite(StringBuilder builder) {
		builder.append(data);
	}
	
	@Override
	public boolean isRawData(){
		return true;
	}

}
