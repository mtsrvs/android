package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.protocol.JID;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls, XMPPProcessorMap xmppProcessorMap) {
		super(readerFactory, filterControls, xmppProcessorMap);
	}

	////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// SASL/DIGEST-MD5 methods
	
	private boolean jidFlag = false;
	private boolean resetMessage = false;
	
	@Override
	protected void handleStartElement(int vLocation) {
		switch(getTagType(getReader().getName().getLocalPart())) {
		case SUCCESS:
			this.markToReset();
			this.messageBuffer.markEventToSend(vLocation);break;
		case JID:
			this.jidFlag = true;
			this.messageBuffer.markEventToSend(vLocation);break;
		default:
			this.messageBuffer.markEventToSend(vLocation);
		}		
	}

	@Override
	protected void handleEndElement(int vLocation) {
		switch(getTagType(getReader().getLocalName())) {
		case JID:
			this.jidFlag = false;
			this.messageBuffer.markEventToSend(vLocation);break;
		default:
			this.messageBuffer.markEventToSend(vLocation);
		}
	}
	
	@Override
	protected void handleCharacters(int vLocation) {
		if (this.jidFlag){
			this.jid = new JID(getReader().getText());
			this.xmppProcessorMap.getXMPPClientProcessor(this).jid = this.jid;
		}
	}	
	
	@Override
	public boolean hasResetMessage() {
		if(resetMessage) {
			this.resetMessage = false;
			return true;
		}
		return false;
	}

	@Override
	public void markToReset() {
		super.markToReset();
		this.resetMessage = true;
	}	
	
	/////////////////////////////////////////////////////////////////////	
	//////////////////////////////////////// NON-SASL methods
	
	/*@Override
	protected void handleStartElement(int vLocation) {
		switch(getTagType(getReader().getLocalName())) {
		case IQ:
			handleIQAttributes();
			this.messageBuffer.markEventToSend(vLocation);
			break;
		default:
			this.messageBuffer.markEventToSend(vLocation);
		}
	}
	
	protected void handleIQAttributes() {
		for(int i=0; i < getReader().getAttributeCount(); i++){
			String attrName = getReader().getAttributeLocalName(i);
			if (attrName.equalsIgnoreCase("type")){
				String attrValue = getReader().getAttributeValue(i);
				if (attrValue.equalsIgnoreCase("result"))
					try {
						if (getReader().isEmptyElement()){
							XMPPClientMessageProcessor cmp  = this.xmppProcessorMap.getXMPPClientProcessor(this);
							this.jid = new JID(cmp.getUsername(), cmp.getServer(), cmp.getResource());
							cmp.jid = this.jid;
						}
					} catch (XMLStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}*/
	

}
