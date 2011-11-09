package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.iharder.Base64;

import org.joda.time.DateTime;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.UserSilencedException;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.ByteStreamsInfo;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filetransfer.XMPPFileInfo;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
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
	
	private List<XMPPFileInfo> files = new LinkedList<XMPPFileInfo>();
	
	public XMPPClientMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			AccessControls accessControls, FileTransferManager fileManager, ConnectionMap connectionMap) {
		super(configLoader, readerFactory, filterControls, accessControls, fileManager, connectionMap);
	}

	public XMPPServerMessageProcessor getEndpoint(){
		return (XMPPServerMessageProcessor) this.endpoint;
	}
	
	@Override
	protected void processXMPPElement(StartElement e) {
		if(e.getName().contains("stream")) {
			String toValue = e.getAttributes().get("to");
			if (toValue != null)
				this.jid.setServer(toValue);
			
			String fromValue = e.getAttributes().get("from");
			if (fromValue != null){
				this.fromAttribute = true;
				this.jid = new JID(fromValue);
				
				/*//Multiplexaci�n si viene el atributo 'from' en el elemento stream inicial.
				try {
					InetSocketAddress multiplex = this.accessControls.multiplex(this.jid.getUsername());
					InetSocketAddress origin = (InetSocketAddress)configLoader.getOriginServer();
					SocketChannel ss = null;
					if(multiplex == null){
						ss = SocketChannel.open(origin);
					} else {
						InetSocketAddress addr = multiplex;
						ss = SocketChannel.open();
						e.getAttributes().put("from", addr.getHostName());
					}
					ss.configureBlocking(false);
					ss.register(this.selector, SelectionKey.OP_READ, this.attach);
					this.connectionMap.addConnection(this.client, ss);
				} catch(IOException exc){
					Isecu.log.debug(exc);
				}*/
			}
		}	
	}
	
	@Override
	public boolean isClientProcessor(){
		return true;
	}
	
	public String getServer(){
		return this.jid.getServer();
	}
	
	public String getUsername(){
		return this.jid.getUsername();
	}
	
	public String getResource(){
		return this.jid.getResource();
	}
	
	private void handleIqSi(SimpleElement si, String id, String type, String to) {
		if(ElemUtils.hasNullValues(si, id, type, to) || !configLoader.checkHash(this.jid.getUserInfo())) {
			return;
		}
		if(type.equalsIgnoreCase("set")) {
			SimpleElement file = si.getFirstChild("file");
			String name = file.getAttribute("name");
			int size = Integer.valueOf(file.getAttribute("size"));
			
			XMPPFileInfo f = new XMPPFileInfo(id, si.getAttribute("id"), this.jid.toString(), to, name, size);
			
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
			files.add(f);
			manageFile(f);
		}
	}

	private void manageFile(XMPPFileInfo file) {
		if(file.supportByteStreamsOrIBB()) {
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

	private void handleByteStreams(SimpleElement query, String id, String type, String to) {
		if(ElemUtils.hasNullValues(query, id, type, to) || !configLoader.checkHash(this.jid.getUserInfo())) {
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
			bsi.setFile(getFileInfoBySid(sid));
			connectToStreamHost(bsi);
			query.notSend();
		}
	}
	
	private void connectToStreamHost(final ByteStreamsInfo bsi) {
		try {
			Socket s = this.fileManager.socks5connect(bsi, 10000);
			bsi.getFile().setProxyIp(s.getLocalAddress().getHostAddress());
			this.fileManager.receiveFile(s, this.endpoint, bsi);
			this.appendOnEndpointBuffer(PredefinedMessages.streamHostUsed(bsi.getId(), bsi.getTo(), bsi.getFrom(), bsi.getJid()));
			Isecu.log.info("File Transfer: Stream initiated[" + bsi.getFrom() + "]");
			((XMPPServerMessageProcessor)this.endpoint).markToWrite();
		}catch (Exception e) {
			Isecu.log.debug(e);
			this.appendOnEndpointBuffer(PredefinedMessages.streamHostFail(bsi.getId(), bsi.getTo(), bsi.getFrom()));
			Isecu.log.info("File Transfer: Strem initiation failed[" + bsi.getFrom() + "]");
		}
	}
	
	@Override
	protected void handleAuthElement(SimpleElement e){
		if(!fromAttribute && ElemUtils.hasTextEquals(e.getNamespaces().get("xmlns"), "urn:ietf:params:xml:ns:xmpp-sasl"))
			handleSASLPlainSession(e);
	}
	
	protected boolean afterMultiplex = false;
	
	private void handleSASLPlainSession(SimpleElement e) {
		String data = e.getBodyAsRawData();
		try {
			String decoded = new String(Base64.decode(data), Charset.forName("UTF-8"));
			String[] parts = decoded.split("\0");
			Isecu.log.debug("Username -> parts[1]:" + parts[1]);
			this.jid.setUsername(parts[1]);
			
			//Multiplexaci�n
			/*InetAddress multiplex = this.accessControls.multiplex(this.jid.getUsername());
			if (multiplex != null){
				
			}*/
			
		} catch (Exception exc) {
			Isecu.log.debug(exc);
		}
	}
	
	private SocketChannel client;
	private Selector selector;
	private ChannelAttach attach;
	public void associateChannel(SocketChannel client, Selector selector, ChannelAttach attach){
		this.client = client;
		this.selector = selector;
		this.attach = attach;
	}
	
	/*@Override
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
			this.jid.setUsername(matcher.group(1));
			
		} catch (Exception exc) {
			Isecu.log.debug(exc);
		}
	}*/
	
	public void handleMessageStanza(MessageStanza messageStanza) {
		updateLastStanzaTime();	
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
		handleByteStreams(iqStanza.getFirstElementWithNamespace("http://jabber.org/protocol/bytestreams"), id, type, to);
	}

	private void handleIqBind(SimpleElement bind) {
		if(bind != null) {
			SimpleElement resource = bind.getFirstChild("resource");
			if (resource != null)
				this.jid.setResource(resource.getFirstTextData());
		}
	}

	public void handleOtherElement(SimpleElement simpleElement) {
		
	}

	private DateTime lastStanzaTime = new DateTime();
	
	private void updateLastStanzaTime(){
		this.lastStanzaTime = new DateTime();
		this.accessControls.reorder(this);
	}

	public int compareTo(XMPPClientMessageProcessor o) {
		return this.lastStanzaTime.compareTo(o.lastStanzaTime);
	}
	
	public String toString(){
		return "Resource: " + this.jid.getResource() + " - " + this.lastStanzaTime.getHourOfDay() + ":" + this.lastStanzaTime.getMinuteOfHour() + ":" + this.lastStanzaTime.getSecondOfMinute() + ":" + this.lastStanzaTime.getMillisOfSecond();
	}
	
	private XMPPFileInfo getFileInfoBySid(String sid) {
		for(XMPPFileInfo f : this.files) {
			if(sid.equals(f.getSid())) {
				return f;
			}
		}
		return null;
	}
	
	public XMPPFileInfo getFileInfoByIdOffer(String id) {
		for(XMPPFileInfo f : this.files) {
			if(id.equals(f.getId())) {
				return f;
			}
		}
		return null;
	}
	
}
