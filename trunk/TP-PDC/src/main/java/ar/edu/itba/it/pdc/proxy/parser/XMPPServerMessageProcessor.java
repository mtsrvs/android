package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;
import ar.edu.itba.it.pdc.proxy.protocol.JID;


public class XMPPServerMessageProcessor extends XMPPMessageProcessor {

	private boolean resetMessage = false;

	public XMPPServerMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, accessControls, xmppProcessorMap);
	}

	@Override
	protected void processXMPPElement(StartElement e) {
		
	}

	@Override
	protected void processXMPPElement(SimpleElement e) {
		if(e.getName().contains("success")) {
			this.markToReset();
			this.resetMessage = true;
		} else if (e.getName().contains("iq")) {
			StartElement s = e.getStartElement();
			String typeValue = s.getAttributes().get("type");
			XMPPClientMessageProcessor cmp = xmppProcessorMap.getXMPPClientProcessor(this);
			
			try {
				if (typeValue != null && e.getBody().isEmpty()
						&& typeValue.equalsIgnoreCase("result")
						&& cmp.getNonSASLFlag()){
					handleNonSASLSession(e);
					cmp.setNonSASLFlag(false);
				} else {
					handleSASLSession(e);
				}
			} catch(AccessControlException acx){
				acx.printStackTrace();
			}
		}
	}
	
	private void handleNonSASLSession(SimpleElement e) throws AccessControlException {
		XMPPClientMessageProcessor cmp = this.xmppProcessorMap.getXMPPClientProcessor(this);
		JID j = new JID(cmp.getUsername(), cmp.getServer(), cmp.getResource());
		
		accessControl(j);
		
		this.jid = j;
		cmp.jid = j;
	}
	
	private void handleSASLSession(SimpleElement e) throws AccessControlException {
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
										JID j = new JID(rd.getData());
										
										accessControl(j);
										
										this.jid = j;
										this.xmppProcessorMap.getXMPPClientProcessor(this).jid = j;
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
}
