package ar.edu.itba.it.pdc.proxy.parser.element;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class XMPPElement {

	public static Charset charset = Charset.forName("UTF-8");
	
	protected SimpleElement parent;
	private byte[] data;
	private int written = 0;
	
	public XMPPElement(SimpleElement parent) {
		super();
		this.parent = parent;
	}

	public void setToWrite() {
		setDataToWrite();
	}
	
	public boolean readyToWrite() {
		return this.data != null;
	}
	
	public boolean write(int i) {
		this.written += i;
		return data.length == this.written;
	}
	
	private void setDataToWrite() {
		StringBuilder builder = new StringBuilder();
		this.appendDataToWrite(builder);
		this.data = builder.toString().getBytes(charset);
	}
	
	public SimpleElement getParent() {
		return parent;
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(data);
	}
	
	public String getData() {
		return new String(data, charset);
	}
	
	protected abstract void appendDataToWrite(StringBuilder builder);
	
	public boolean isXMPPElement(){
		return true;
	}
	
	public boolean isSimpleElement(){
		return false;
	}
	
	public SimpleElement asSimpleElement() {
		return (SimpleElement) this;
	}
	
	public RawData asRawData() {
		return (RawData) this;
	}
	
	public boolean isRawData(){
		return false;
	}
	
	public boolean isStartDocumentElement(){
		return false;
	}
	
	public boolean isStartElement(){
		return false;
	}
}
