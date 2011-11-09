package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.List;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	private boolean forceWrite = false;
	
	private ServerSocketChannel channel;
	public void associateChannel(ServerSocketChannel server){
		this.channel = server;
	}
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, FileTransferManager fileManager, ConnectionMap connectionMap) {
		super(configLoader, readerFactory, filterControls, accessControls, fileManager, connectionMap);
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
			handleIqBind(iqStanza.getFirstElementWithNamespace("urn:ietf:params:xml:ns:xmpp-bind"));
		}
		handleSiResult(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/si"), iqStanza.getAttribute("id"), iqStanza.getAttribute("type"));
	}	
	
	@Override
	public void handleStreamFeatures(SimpleElement e){
		SimpleElement mechanisms = e.getFirstElementWithNamespace("urn:ietf:params:xml:ns:xmpp-sasl");
		
		if (mechanisms != null){
			List<SimpleElement> mechanismList = mechanisms.getChildren("mechanism");
			Iterator<SimpleElement> iterator = mechanismList.iterator();
			while(iterator.hasNext()){
				SimpleElement elem = iterator.next();
				if (fromAttribute && !ElemUtils.hasTextEquals(elem.getFirstTextData(), "DIGEST-MD5"))
					iterator.remove();
				else if (!fromAttribute && !ElemUtils.hasTextEquals(elem.getFirstTextData(), "PLAIN") )
					iterator.remove();
			}
			
			mechanisms.setChildren("mechanism", mechanismList);
			
			if (mechanisms.getChildren("mechanism").isEmpty()){
				String mech = fromAttribute ? "DIGEST-MD5" : "PLAIN";
				String message = "Authentication via SASL/" + mech + " mechanism not supported by this server";
				Isecu.log.info(message);
				bufferAdd(sc.handleUserControlException("temporary-auth-failure", message));
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
			
			this.fromAttribute = true;
			
			// Time range/Max Logins access controls
			try {
				this.accessControls.logins(getEndpoint().getUsername());
				this.accessControls.range(getEndpoint().getUsername());				
			} catch (MaxLoginsAllowedException exc){
				Isecu.log.info("Access denied: " + exc.getMessage());
				buffer.clear();
				bufferAdd(sc.handleUserControlException("not-authorized", exc.getMessage()));
				simpleElement.notSend();
				return;
			} catch (InvalidRangeException exc) {
				Isecu.log.info("Access denied: " + exc.getMessage());
				buffer.clear();
				bufferAdd(sc.handleUserControlException("not-authorized", exc.getMessage()));
				simpleElement.notSend();
				return;
			}
						
			this.jid = this.endpoint.jid;
			this.markToReset();
			this.resetMessage = true;
			Isecu.log.info("User connection[" + this.jid + "]");
		}
	}

	@Override
	protected void processXMPPElement(StartElement e) {
		
	}
	
	private void handleIqBind(SimpleElement bind){
		if (bind != null){
			SimpleElement j = bind.getFirstChild("jid");
			if (j != null){
				this.jid.setResource(getEndpoint().getResource());
				Isecu.log.debug("Resource binded for " + this.jid.getUsername() + ": " + this.jid.getResource());
				
				List<XMPPClientMessageProcessor> cmps = this.accessControls.concurrentSessions(getEndpoint());
				if (cmps != null){					
					for (XMPPClientMessageProcessor cmp : cmps){
						String message = "Maximum amount of concurrent sessions for user '" + this.jid.getUsername() + "' reached. ";
						message += "Closing most inactive resources for '" + this.jid.getUsername() + "'.";
						cmp.getEndpoint().bufferAdd(sc.handleStreamError("conflict", message));
						cmp.getEndpoint().stopAddingToBuffer();
						Isecu.log.info("Max concurrent sessions for " + jid.getUsername() + ": Resource: " + cmp.jid.getResource() + " closed.");
					}
				}
			}
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
