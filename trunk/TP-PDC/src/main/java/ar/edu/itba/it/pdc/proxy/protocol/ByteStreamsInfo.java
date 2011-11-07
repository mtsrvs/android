package ar.edu.itba.it.pdc.proxy.protocol;

public class ByteStreamsInfo {
	
	private String id;
	
	private String from;
	private String to;
	
	private String mode = "tcp";
	private String sid;
	
	private String port;
	private String host;
	private String jid;
	
	public ByteStreamsInfo(String id, String from, String to, String sid, String jid, String host, String port) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.sid = sid;
		this.jid = jid;
		this.host = host;
		this.port = port;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getId() {
		return id;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getMode() {
		return mode;
	}

	public String getSid() {
		return sid;
	}

	public String getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getJid() {
		return jid;
	}

	@Override
	public String toString() {
		return "ByteStreamsInfo [id=" + id + ", from=" + from + ", to=" + to
				+ ", mode=" + mode + ", sid=" + sid + ", port=" + port
				+ ", host=" + host + ", jid=" + jid + "]";
	}
	
}
