package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.filters.L33tFilter;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	private boolean authFlag = false;	
	private boolean bodyFlag = false;
	
	public XMPPClientMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory, FilterControls filterControls, XMPPProcessorMap xmppProcessorMap) {
		super(readerFactory, filterControls, xmppProcessorMap);
	}
	
	@Override
	protected void handleStartElement(int vLocation){
		switch(getTagType(getReader().getLocalName())){
		case AUTH:
			this.authFlag = true; break;
		case BODY:
			this.bodyFlag = true; break;
		default:
			break;
		}
		
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	@Override
	protected void handleEndElement(int vLocation) {
		switch(getTagType(getReader().getLocalName())){
		case AUTH:
			this.authFlag = false; break;
		case BODY:
			this.bodyFlag = false; break;
		default:
			break;
		}
		
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	@Override
	protected void handleCharacters(int vLocation) {
		if (this.bodyFlag)
			handleBodyTag();		
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	private void handleAuthAttributes(int vLocation) {
		String ns = getReader().getNamespaceURI();
		if (ns.equalsIgnoreCase("urn:ietf:params:xml:ns:xmpp-sasl")){
			for(int i=0; i < getReader().getAttributeCount(); i++){
				String attr = getReader().getAttributeLocalName(i);
				if (attr.equalsIgnoreCase("mechanisms")){
					String attrValue = getReader().getAttributeValue(i);
					if (attrValue.equals("DIGEST-MD5")){
						this.digestMD5Flag = true;
						this.xmppProcessorMap.getXMPPServerProcessor(this).digestMD5Flag = true;
						
						//TODO la idea es que el servermessageprocessor procese sólo si se trata de 
						// SASL/DIGEST o NON-SASL.
						// onda un if con digestMD5Flag. En cualquier otro caso, el proxy devuelve
						// un mensaje diciendo que no puedo trabajar con esos clientes/servers.
					}
				}
			}
		}
	}
	
	/*
	////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////// NON SASL
	private boolean auFlag = false;
	private boolean usernameFlag = false;
	private boolean resourceFlag = false;
	private String username = null;
	private String server = null;
	private String resource = null;
	
	@Override
	protected void handleStartElement(int vLocation){
		switch(getTagType(getReader().getLocalName())){
		case STREAM:
			handleStreamAttributes(); break;
		case QUERY:
			handleQueryAttributes(); break;
		case USERNAME:
			this.usernameFlag = this.auFlag; break;
		case RESOURCE:
			this.resourceFlag = this.auFlag; break;
		case BODY:
			this.bodyFlag = true; break;
		default:
			break;
		}
		
		this.messageBuffer.markEventToSend(vLocation);
	}

	@Override
	protected void handleEndElement(int vLocation){
		switch(getTagType(getReader().getLocalName())){
		case USERNAME:
			this.usernameFlag = false; break;
		case RESOURCE:
			this.resourceFlag = false; break;
		case BODY:
			this.bodyFlag = false; break;
		default:
			break;
		}
		
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	@Override
	protected void handleCharacters(int vLocation) {
		if(this.usernameFlag)
			this.username = getReader().getText();
		else if (this.resourceFlag)
			this.resource = getReader().getText();
		else if (this.bodyFlag)
			handleBodyTag();
		
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	private void handleQueryAttributes() {
		String ns = getReader().getNamespaceURI();
		this.auFlag = ns.equalsIgnoreCase("jabber:iq:auth");
	}
	
	private void handleStreamAttributes() {
		for (int i=0; i < getReader().getAttributeCount(); i++){
			if (getReader().getAttributeLocalName(i).equalsIgnoreCase("to"))
				this.server = getReader().getAttributeValue(i);
		}
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getServer(){
		return this.server;
	}
	
	public String getResource(){
		return this.resource;
	}*/
	
	/////////////////////////////////////////////////////////////////
	
	private void handleBodyTag(){
		System.out.println(jid);
		if (this.filterControls.l33t(this.jid)){
			String body = getReader().getText();
			
			int start = this.messageBuffer.getLastEvent() - this.messageBuffer.getConsumed();
			int end = start + body.length();

			this.messageBuffer.replace(start, end, L33tFilter.transform(body));
		}
	}
}
