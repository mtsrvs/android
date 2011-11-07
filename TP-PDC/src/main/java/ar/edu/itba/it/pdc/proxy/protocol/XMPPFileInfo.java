package ar.edu.itba.it.pdc.proxy.protocol;

import java.util.LinkedList;
import java.util.List;

public class XMPPFileInfo {

	private String id;
	
	private String originHost;
	private int originPort;
	private String jid;
	
	private int proxyPort;
	
	private String name;
	private int size;
	
	private String desc;
	private String date;
	private String hash;
	
	private List<String> streamMethods = new LinkedList<String>();
	
	public XMPPFileInfo(String id, String name, int size) {
		this.id = id;
		this.size = size;
		this.name = name;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getDate() {
		return date;
	}
	
	public String getHash() {
		return hash;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return originHost;
	}

	public void setHost(String host) {
		this.originHost = host;
	}

	public int getPort() {
		return originPort;
	}

	public void setPort(int port) {
		this.originPort = port;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void addStreamMethod(String method) {
		this.streamMethods.add(method);
	}
	
	public List<String> getStreamMethods() {
		return streamMethods;
	}

	public boolean supportByteStreamsOrIBB() {
		for(String sm : this.streamMethods) {
			if(sm.equals("http://jabber.org/protocol/bytestreams") || sm.equals("http://jabber.org/protocol/ibb")) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "XMPPFileInfo [id=" + id + ", originHost=" + originHost
				+ ", originPort=" + originPort + ", jid=" + jid
				+ ", proxyPort=" + proxyPort + ", description=" + desc
				+ ", size=" + size + ", name=" + name + ", date=" + date
				+ ", hash=" + hash + "]";
	}
	
}
