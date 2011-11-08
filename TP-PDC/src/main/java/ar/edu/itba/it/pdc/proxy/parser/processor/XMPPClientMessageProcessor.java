package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.iharder.Base64;

import org.joda.time.DateTime;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
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
	private String resource = null;
	
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
	
	public String getResource(){
		return this.resource;
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
			query.notSend();
		}
	}
	
	private void connectToStreamHost(final ByteStreamsInfo bsi) {
		try {
			Socket s = this.fileManager.socks5connect(bsi, 10000);
			this.fileManager.receiveFile(s);
			this.appendOnEndpointBuffer(PredefinedMessages.streamHostUsed(bsi.getId(), bsi.getTo(), bsi.getFrom(), bsi.getJid()));
			Isecu.log.info("File Transfer: Stream initiated[" + bsi.getFrom() + "]");
		}catch (Exception e) {
			Isecu.log.debug(e);
			this.appendOnEndpointBuffer(PredefinedMessages.streamHostFail(bsi.getId(), bsi.getTo(), bsi.getFrom()));
			Isecu.log.info("File Transfer: Strem initiation failed[" + bsi.getFrom() + "]");
		}
	}
	
	@Override
	protected void handleResponseElement(SimpleElement e){
		if(ElemUtils.hasTextEquals(e.getNamespaces().get("xmlns"), "urn:ietf:params:xml:ns:xmpp-sasl"))
			handleSASLSession(e);
	}
	
	private void handleSASLSession(SimpleElement e) {
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
			appendOnEndpointBuffer(sc.handleUserControlException("not-authorized", exc.getMessage()));
			e.notSend();
		} 
	}
	
	public void handleMessageStanza(MessageStanza messageStanza) {
		updateLastStanzaTime();	
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
		updateLastStanzaTime();
	}

	public void handleIqStanza(IQStanza iqStanza) {
		updateLastStanzaTime();
		String type = iqStanza.getAttribute("type");
		String id = iqStanza.getAttribute("id");
		String to = iqStanza.getAttribute("to");
		if (ElemUtils.hasTextEquals(type, "set"))
			handleIqBind(iqStanza.getFirstElementWithNamespace("urn:ietf:params:xml:ns:xmpp-bind"));
		handleIqSi(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/si"), id, type, to);
	}

	private void handleIqBind(SimpleElement bind) {
		if(bind != null) {
			SimpleElement resource = bind.getFirstChild("resource");
			if (resource != null)
				this.resource = resource.getFirstTextData();
		}
	}

	public void handleOtherElement(SimpleElement simpleElement) {
		
	}

	private DateTime lastStanzaTime = new DateTime();
	
	private void updateLastStanzaTime(){
		this.lastStanzaTime = new DateTime();
	}

	public int compareTo(XMPPClientMessageProcessor o) {
		return this.lastStanzaTime.compareTo(o.lastStanzaTime);
	}
	
	public String toString(){
		if (this.jid == null)
			return "null - " + this.lastStanzaTime.getHourOfDay() + ":" + this.lastStanzaTime.getMinuteOfHour() + ":" + this.lastStanzaTime.getSecondOfMinute();
		return "Resource: " + this.jid.getResource() + " - " + this.lastStanzaTime.getHourOfDay() + ":" + this.lastStanzaTime.getMinuteOfHour() + ":" + this.lastStanzaTime.getSecondOfMinute();
	}
	
}
