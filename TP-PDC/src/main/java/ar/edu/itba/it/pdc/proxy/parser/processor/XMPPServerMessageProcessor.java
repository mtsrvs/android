package ar.edu.itba.it.pdc.proxy.parser.processor;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;
import ar.edu.itba.it.pdc.proxy.protocol.JID;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;

	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, xmppProcessorMap);
	}

	@Override
	protected void processXMPPElement(StartElement e) {
		
	}

	private void handleNonSASLSession(SimpleElement e){
		XMPPClientMessageProcessor cmp = this.xmppProcessorMap.getXMPPClientProcessor(this);
		this.jid = new JID(cmp.getUsername(), cmp.getServer(), cmp.getResource());
		cmp.jid = this.jid;
	}
	
	private void handleSASLSession(SimpleElement e){
		for (XMPPElement elem1 : e.getBody())
			if (elem1.isSimpleElement()){
				SimpleElement e1 = ((SimpleElement) elem1);
				if (e1.getName().equalsIgnoreCase("bind"))
					for (XMPPElement elem2 : e1.getBody())
						if (elem2.isSimpleElement()){
							SimpleElement e2 = (SimpleElement) elem2;
							if (e2.getName().equalsIgnoreCase("jid"))
								for (XMPPElement elem3 : e2.getBody())
									if (elem3.isRawData()){
										RawData rd = (RawData) elem3;
										System.out.println("HOLAAAAAAAA");
										this.jid = new JID(rd.getData());System.out.println(jid);
										this.xmppProcessorMap.getXMPPClientProcessor(this).jid = this.jid;
									}
						}
			}
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

	public void handleIqStanza(IQStanza iqStanza) {
		StartElement s = iqStanza.getStartElement();
		String typeValue = s.getAttributes().get("type");
		XMPPClientMessageProcessor cmp = xmppProcessorMap.getXMPPClientProcessor(this);
		if (typeValue != null && iqStanza.getBody().isEmpty()
				&& typeValue.equalsIgnoreCase("result")
				&& cmp.getNonSASLFlag()){
			handleNonSASLSession(iqStanza);
			cmp.setNonSASLFlag(false);
		} else {
			handleSASLSession(iqStanza);
		}
	}

	public void handleMessageStanza(MessageStanza messageStanza) {
		// TODO Auto-generated method stub
		
	}

	public void handlePresenceStanza(PresenceStanza presenceStanza) {
		// TODO Auto-generated method stub
		
	}

	public void handleOtherElement(SimpleElement simpleElement) {
		if(simpleElement.getName().contains("success")) {
			this.markToReset();
			this.resetMessage = true;
		}
	}
}
