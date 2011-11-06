package ar.edu.itba.it.pdc.proxy.protocol;

public class XMPPFileInfo {

	private String id;
	private String sid;
	
	private String originHost;
	private int originPort;
	private String jid;
	
	private int proxyPort;
	
	
	private String description;
	private int size;
	private String name;
	private String date;
	private String hash;
	
	public XMPPFileInfo(String id, String sid, String desc, int size, String name, String date, String hash) {
		this.id = id;
		this.sid = sid;
		this.description = desc;
		this.size = size;
		this.name = name;
		this.date = date;
		this.hash = hash;
	}
	
	public String getDescription() {
		return description;
	}

	public int getSize() {
		return size;
	}
	
	public String getName() {
		return name;
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

	public String getSid() {
		return sid;
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

	@Override
	public String toString() {
		return "XMPPFileInfo [id=" + id + ", sid=" + sid + ", host=" + originHost
				+ ", port=" + originPort + ", jid=" + jid + ", description="
				+ description + ", size=" + size + ", name=" + name + ", date="
				+ date + ", hash=" + hash + "]";
	}
	
}
