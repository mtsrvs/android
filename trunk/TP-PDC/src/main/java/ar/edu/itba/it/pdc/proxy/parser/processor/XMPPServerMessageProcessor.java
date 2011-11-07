package ar.edu.itba.it.pdc.proxy.parser.processor;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.protocol.JID;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;
	
	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls) {
		super(configLoader, readerFactory, filterControls, accessControls);
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

	public void handleIqStanza(IQStanza iqStanza) throws AccessControlException {
		// TODO Auto-generated method stub
		
	}

	public void handleOtherElement(SimpleElement simpleElement) throws MaxLoginsAllowedException {
		if(simpleElement.getName().equalsIgnoreCase("success")) {
			try {
				this.accessControls.logins(getEndpoint().getUsername());
			} catch (MaxLoginsAllowedException exc){
				Isecu.log.info("Access denied: " + exc.getMessage());
				buffer.clear();
				buffer.add(sc.handleUserControlException("invalid-from", exc.getMessage()));
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
}
