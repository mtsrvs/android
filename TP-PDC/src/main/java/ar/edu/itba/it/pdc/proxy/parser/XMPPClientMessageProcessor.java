package ar.edu.itba.it.pdc.proxy.parser;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.element.RawData;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	public XMPPClientMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, XMPPProcessorMap xmppProcessorMap) {
		super(configLoader, readerFactory, filterControls, accessControls, xmppProcessorMap);
	}

	private String server = null;
	private String username = null;
	private String resource = null;
	private boolean nonSASLFlag = false;
	
	@Override
	protected void processXMPPElement(SimpleElement e) {
		if (e.getName().contains("iq")) {
			for (XMPPElement elem1 : e.getBody()){
				if (elem1.isSimpleElement()){
					SimpleElement e1 = ((SimpleElement) elem1);
					if (e1.getName().equalsIgnoreCase("query")){
						String ns = e1.getStartElement().getNamespaces().get("xmlns");
						if (ns != null && ns.equalsIgnoreCase("jabber:iq:auth")){
							for (XMPPElement elem2 : e1.getBody())
								if (elem2.isSimpleElement()){
									SimpleElement e2 = (SimpleElement) elem2;
									if (e2.getName().equalsIgnoreCase("username")){
										for (XMPPElement elem3 : e2.getBody())
											if (elem3.isRawData()){
												RawData rd = (RawData) elem3;
												this.username = rd.getData();
												this.nonSASLFlag = true;
											}
									} else if (e2.getName().equalsIgnoreCase("resource")){
										for (XMPPElement elem3 : e2.getBody())
											if (elem3.isRawData()){
												RawData rd = (RawData) elem3;
												this.resource = rd.getData();
												this.nonSASLFlag = true;
											}
									}
								}
						}
					}
				}
			}
		}
	}
	
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
	
}
