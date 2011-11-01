package ar.edu.itba.it.pdc.proxy.parser.element;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.it.pdc.exception.InvalidProtocolException;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class StreamConstructor {

	private SimpleElement currentElement;
	
	/**
	 * Maneja el evento de inicio de documento.
	 * @param r
	 * @return
	 */
	public XMPPElement handleStartDocument(AsyncXMLStreamReader r) {
		return new StartDocumentElement(null, r.getVersion(), r.getEncoding());
	}
	
	/**
	 * Maneja el evento de comienzo de elemento. En caso de que sea el elemento
	 * stream lo retorna. Si es otro elemento, lo conserva para seguir construyendolo
	 * y retorna null.
	 * @param Reader actual
	 * @return null o StartElement
	 */
	public XMPPElement handleStartElement(AsyncXMLStreamReader r) {
		
		if(this.currentElement != null && !(this.currentElement instanceof SimpleElement)){
			throw new InvalidProtocolException("Invalid stream");
		}
		
		
		String prefix = r.getName().getPrefix();
		String name = r.getName().getLocalPart();
		StartElement se = new StartElement((SimpleElement) this.currentElement, name, prefix, getAttributes(r), getNamespaces(r));
		
		if(isStreamStarter(r)) {
			return se;
		}else{
			this.currentElement = isStanza(r) ? new Stanza((SimpleElement)this.currentElement, se) : new SimpleElement((SimpleElement)this.currentElement, se);
		}
		return null;
		
	}
	
	private boolean isStanza(AsyncXMLStreamReader r) {
		return r.getName().getLocalPart().equalsIgnoreCase("message") ||
		r.getName().getLocalPart().equalsIgnoreCase("iq") ||
		r.getName().getLocalPart().equalsIgnoreCase("presence");
	}
	
	private boolean isStreamStarter(AsyncXMLStreamReader r) {
		return r.getName().getLocalPart().equalsIgnoreCase("stream") && 
		r.getName().getPrefix().equalsIgnoreCase("stream");
	}
	
	private Map<String, String> getAttributes(AsyncXMLStreamReader r) {
		Map<String, String> ret = new HashMap<String, String>();
		int qattr = r.getAttributeCount();
		for(int i = 0; i < qattr; i++) {
			String prefix = r.getAttributeName(i).getPrefix();
			String name;
			if(prefix != null && !prefix.isEmpty()) {
				name = prefix + ":" + r.getAttributeName(i).getLocalPart();
			}else{
				name = r.getAttributeName(i).getLocalPart();
			}
			ret.put(name , r.getAttributeValue(i));
		}
		return ret;
	}
	
	private Map<String, String> getNamespaces(AsyncXMLStreamReader r) {
		Map<String, String> ret = new HashMap<String, String>();
		int qns= r.getNamespaceCount();
		for(int i = 0; i < qns; i++) {
			String prefix = r.getNamespacePrefix(i);
			String name;
			if(prefix != null && !prefix.isEmpty()) {
				name = "xmlns:" + prefix;
			}else{
				name = "xmlns";
			}
			ret.put(name , r.getNamespaceURI(i));
		}
		return ret;
	}

	/**
	 * Maneja el evento de finalización de elemento.
	 * @param r
	 * @return
	 */
	public XMPPElement handleEndElement(AsyncXMLStreamReader r) {
		SimpleElement ret = this.currentElement;
		this.currentElement = this.currentElement.getParent();
		if(this.currentElement == null) {
			return ret;
		}else{
			this.currentElement.appendBody(ret);
			return null;
		}
	}
	
	public XMPPElement handleCharacters(AsyncXMLStreamReader r) {
		String text = r.getText();
		if(text != null && !text.isEmpty()) {
			if(this.currentElement == null) {
				return new RawData(this.currentElement, r.getText());
			}else{
				this.currentElement.appendBody(new RawData(this.currentElement, r.getText()));
				return null;
			}
		}
		return null;
	}
	
	public void handleOtherEvent(AsyncXMLStreamReader r) {
		throw new InvalidProtocolException("Unexpected text");
	}
	
	public void reset() {
		this.currentElement = null;
	}
}
