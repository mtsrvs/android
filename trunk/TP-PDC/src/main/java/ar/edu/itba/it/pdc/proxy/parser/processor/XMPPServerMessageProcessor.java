package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.util.Iterator;
import java.util.List;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils;
import ar.edu.itba.it.pdc.proxy.protocol.JID;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	private boolean forceWrite = false;
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, FileTransferManager fileManager) {
		super(configLoader, readerFactory, filterControls, accessControls, fileManager);
	}

	@Override
	protected void processXMPPElement(StartElement e) {
	}

	private XMPPClientMessageProcessor getEndpoint(){
		return (XMPPClientMessageProcessor)this.endpoint;
	}

	@Override
	public boolean hasResetMessage() {
		if(this.resetMessage) {
			this.resetMessage = false;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isServerProcessor(){
		return true;
	}

	public void handleMessageStanza(MessageStanza messageStanza) {
		
	}

	public void handlePresenceStanza(PresenceStanza presenceStanza) {
		
	}

	public void handleIqStanza(IQStanza iqStanza) {
		if (ElemUtils.hasTextEquals(iqStanza.getAttribute("type"), "result")){
			System.out.println("JID: " + jid.getResource());
			System.out.println("from: " + iqStanza.getAttribute("from"));
			handleIqBind(iqStanza.getFirstElementWithNamespace("urn:ietf:params:xml:ns:xmpp-bind"));
		}
		handleSiResult(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/si"), iqStanza.getAttribute("id"), iqStanza.getAttribute("type"));
	}
	
	private void handleIqBind(SimpleElement bind){
		if (bind != null){
			SimpleElement j = bind.getFirstChild("jid");
			if (j != null){
				this.jid.setResource(getEndpoint().getResource());
				Isecu.log.debug("Resource binded for " + this.jid.getUsername() + ": " + this.jid.getResource());
				/*XMPPClientMessageProcessor cmp = this.accessControls.concurrentSessions(this.jid, getEndpoint());
				if (cmp != null){
					cmp.getEndpoint().buffer.clear();
					StreamError pu = sc.handleStreamError("conflict", "HOLAAAAAA");
					cmp.getEndpoint().buffer.add(pu);
					//System.out.println(cmp.buffer);
					Isecu.log.info("Max concurrent sessions: Resource: " + cmp.jid.getResource() + " closed.");
					return;
				}*/
			}
		}
	}	
	
	@Override
	public void handleStreamFeatures(SimpleElement e){
		SimpleElement mechanisms = e.getFirstElementWithNamespace("urn:ietf:params:xml:ns:xmpp-sasl");
		
		if (mechanisms != null){
			List<SimpleElement> mechanismList = mechanisms.getChildren("mechanism");
			Iterator<SimpleElement> iterator = mechanismList.iterator();
			while(iterator.hasNext()){
				SimpleElement elem = iterator.next();
				if (!ElemUtils.hasTextEquals(elem.getFirstTextData(), "DIGEST-MD5"))
					iterator.remove();
			}
			
			mechanisms.setChildren("mechanism", mechanismList);
			
			if (mechanisms.getChildren("mechanism").isEmpty()){
				String message = "DIGEST-MD5 mechanism not supported by this server";
				Isecu.log.info(message);
				buffer.add(sc.handleUserControlException("temporary-auth-failure", message));
			}
		}
	}

	private void handleSiResult(SimpleElement si, String id, String type) {
		if(ElemUtils.hasNullValues(si, id, type)) {
			return;
		}
		if(type.equals("result")) {
			Isecu.log.debug("LLEGA RESULT SI");
			this.forceWrite = false;
		}
	}
	
	public void handleOtherElement(SimpleElement simpleElement) {
		if(simpleElement.getName().equalsIgnoreCase("success")) {
			try {
				this.accessControls.logins(getEndpoint().getUsername());
			} catch (MaxLoginsAllowedException exc){
				Isecu.log.info("Access denied: " + exc.getMessage());
				buffer.clear();
				buffer.add(sc.handleUserControlException("not-authorized", exc.getMessage()));
				return;
			} 
						
			JID jid = new JID(getEndpoint().getUsername(), getEndpoint().getServer());
			this.jid = jid;
			this.endpoint.jid = jid;
			this.markToReset();
			this.resetMessage = true;
			
			Isecu.log.info("User connection[" + this.jid + "]");
		}
	}
	
	public void markToWrite() {
		this.forceWrite = true;
	}

	@Override
	public boolean needToWrite() {
		if(this.forceWrite) {
			return true;
		}
		return super.needToWrite();
	}

	
	
}
