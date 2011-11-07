package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.iharder.Base64;
import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.UserSilencedException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.ByteStreamsInfo;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filetransfer.XMPPFileInfo;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils;
import ar.edu.itba.it.pdc.proxy.parser.element.util.PredefinedMessages;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

public class XMPPClientMessageProcessor extends XMPPMessageProcessor {

	private String server = null;
	private String username = null;
	
	public XMPPClientMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, FileTransferManager fileManager) {
		super(configLoader, readerFactory, filterControls, accessControls, fileManager);
	}

	public XMPPServerMessageProcessor getEndpoint(){
		return (XMPPServerMessageProcessor) this.endpoint;
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

	public void handleIqStanza(IQStanza iqStanza) throws AccessControlException {
		String type = iqStanza.getAttribute("type");
		String id = iqStanza.getAttribute("id");
		String to = iqStanza.getAttribute("to");
		handleIqBind(iqStanza.getFirstChild("bind"));
		handleIqSi(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/si"), id, type, to);
		handleByteStreams(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/bytestreams"), id, type, to);
	}

	private void handleIqBind(SimpleElement bind) {
		if(bind != null) {
			if(bind.getNamespaces().containsValue("urn:ietf:params:xml:ns:xmpp-bind")) {
				SimpleElement resource = bind.getFirstChild("resource");
				this.jid.setResource(resource.getFirstTextData());
			}
		}
	}
	
	private void handleIqSi(SimpleElement si, String id, String type, String to) {
		if(ElemUtils.hasNullValues(si, id, type, to)) {
			return;
		}
		if(type.equalsIgnoreCase("set")) {
			SimpleElement file = si.getFirstChild("file");
			String name = file.getAttribute("name");
			int size = Integer.valueOf(file.getAttribute("size"));
			
			XMPPFileInfo f = new XMPPFileInfo(id, this.jid.toString(), to, name, size);
			
			f.setDate(file.getAttribute("date"));
			f.setHash(file.getAttribute("hash"));
			SimpleElement desc = file.getFirstChild("desc");
			if(desc != null) {
				f.setDesc(desc.getBodyAsRawData());
			}
			si.notSend();
			SimpleElement features = si.getFirstElementWithNamespace("http://jabber.org/protocol/feature-neg");
			SimpleElement x = features.getFirstElementWithNamespace("jabber:x:data");
			SimpleElement field = x.getFirstChild("field");
			List<SimpleElement> options = field.getChildren("option");
			for(SimpleElement opt : options) {
				f.addStreamMethod(opt.getFirstChild("value").getBodyAsRawData());
			}
			manageFile(f);
		}
	}

	private void manageFile(XMPPFileInfo file) {
		if(file.supportByteStreamsOrIBB() && validateServerStreamMethods(file)) {
			XMPPElement siResult = PredefinedMessages.siFileTransferResult(file.getId(), file.getFrom(), file.getTo(), file.getPreferedStreamMethod());
			this.appendOnEndpointBuffer(siResult);
		}else{
			cancelFileNegociation(file);
		}
	}
	
	private void cancelFileNegociation(XMPPFileInfo file) {
		XMPPElement error = PredefinedMessages.notSupportedFeature(file.getId(), file.getTo(), this.jid.toString());
		this.appendOnEndpointBuffer(error);
	}
	
	private boolean validateServerStreamMethods(XMPPFileInfo file) {
		//TODO
		return true;
	}

	private void handleByteStreams(SimpleElement query, String id, String type, String to) {
		if(ElemUtils.hasNullValues(query, id, type, to)) {
			return;
		}
		if(type.equalsIgnoreCase("set")) {
			String sid = query.getAttribute("sid");
			SimpleElement streamhost = query.getFirstChild("streamhost");
			String port = streamhost.getAttribute("port");
			String host = streamhost.getAttribute("host");
			String jid = streamhost.getAttribute("jid");
			ByteStreamsInfo bsi = new ByteStreamsInfo(id, this.jid.toString(), to, sid, jid, host, port);
			bsi.setMode(query.getAttribute("mode"));
			connectToStreamHost(bsi);
		}
	}
	
	private void connectToStreamHost(final ByteStreamsInfo bsi) {
		//TODO: Timeout configurable
		try {
			Socket s = this.fileManager.socks5connection(bsi, 10000);
			this.appendOnEndpointBuffer(PredefinedMessages.streamHostUsed(bsi.getId(), bsi.getTo(), bsi.getFrom(), bsi.getJid()));
		}catch (Exception e) {
			//TODO: mensaje de error en streamhost
			Isecu.log.debug(e);
		}
		
	}
	
	@Override
	protected void handleResponseElement(SimpleElement e) throws AccessControlException{
		if(ElemUtils.hasTextEquals(e.getNamespaces().get("xmlns"), "urn:ietf:params:xml:ns:xmpp-sasl"))
			handleSASLSession(e);
	}
	
	private void handleSASLSession(SimpleElement e) throws AccessControlException {
		String data = e.getBodyAsRawData();
		try {
			String decoded = new String(Base64.decode(data), Charset.forName("UTF-8"));
			Pattern pattern = Pattern.compile("username=\\\"(.*?)\\\"");
			Matcher matcher = pattern.matcher(decoded);
			matcher.find();
			this.username = matcher.group(1);
			
		} catch (Exception exc) {
			Isecu.log.debug(exc);
		}
			
		try {
			this.accessControls.range(this.username);
		} catch (InvalidRangeException exc) {
			Isecu.log.info("Access denied: " + exc.getMessage());
			clearEndpointBuffer();
			appendOnEndpointBuffer(sc.handleUserControlException("invalid-from", exc.getMessage()));
			e.notSend();
		} 
	}
	
	public void handleMessageStanza(MessageStanza messageStanza) {		
		if (this.jid != null){
			JID to = new JID(messageStanza.getTo());
			try {
				this.accessControls.silencerFrom(this.jid.getUsername());
				this.accessControls.silencerTo(to.getUsername());
			} catch (UserSilencedException e) {
				clearEndpointBuffer();
				appendOnEndpointBuffer(sc.handleUserSilencedException(this.jid.toString(), e.getMessage()));
				messageStanza.notSend();
			}
		}
	}

	public void handlePresenceStanza(PresenceStanza presenceStanza) {
		
	}

	public void handleOtherElement(SimpleElement simpleElement) {
		
	}
	
}
