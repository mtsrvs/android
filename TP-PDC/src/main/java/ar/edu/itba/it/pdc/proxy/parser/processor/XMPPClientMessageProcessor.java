package ar.edu.itba.it.pdc.proxy.parser.processor;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, xmppProcessorMap);
	}

	private String server = null;
	private String username = null;
	private String resource = null;
	private boolean nonSASLFlag = false;
	
	@Override
	protected void processXMPPElement(StartElement e) {
		if(e.getName().contains("stream")) {
			String toValue = e.getAttributes().get("to");
			if (toValue != null)
				this.server = toValue;
		}	
	}
	
	@Override
	public boolean isClientProcessor(){
		return true;
	}
	
	public String getServer(){
		return this.server;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getResource(){
		return this.resource;
	}
	
	public boolean getNonSASLFlag(){
		return this.nonSASLFlag;
	}
	
	public void setNonSASLFlag(boolean f){
		this.nonSASLFlag = f;
	}

	public void handleIqStanza(IQStanza iqStanza) {
		handleIqQuery(iqStanza.getFirstChild("query"));
	}

	private void handleIqQuery(SimpleElement query) {
		if(query != null) {
			if(ElemUtils.hasTextEquals(query.getStartElement().getNamespaces().get("xmlns"), "jabber:iq:auth")) {
				SimpleElement username = query.getFirstChild("username");
				this.username = username == null ? null : username.getFirstTextData();
				SimpleElement resource = query.getFirstChild("resource");
				this.resource = resource == null ? null : resource.getFirstTextData();
				this.nonSASLFlag = true;
			}
		}
	}
	
	public void handleMessageStanza(MessageStanza messageStanza) {
		
	}

	public void handlePresenceStanza(PresenceStanza presenceStanza) {
		
	}

	public void handleOtherElement(SimpleElement simpleElement) {
		
	}
	
}
