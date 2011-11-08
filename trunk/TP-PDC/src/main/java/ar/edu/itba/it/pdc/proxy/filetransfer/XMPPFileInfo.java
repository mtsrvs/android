package ar.edu.itba.it.pdc.proxy.filetransfer;

import java.util.LinkedList;
import java.util.List;

public class XMPPFileInfo {

	private String id;
	
	private String sid;
	
	private String from;
	private String to;
	
	private String name;
	private int size;
	
	private String desc;
	private String date;
	private String hash;
	
	private List<String> streamMethods = new LinkedList<String>();
	
	public XMPPFileInfo(String id, String sid, String from, String to, String name, int size) {
		this.id = id;
		this.sid = sid;
		this.from = from;
		this.to = to;
		this.size = size;
		this.name = name;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
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
	
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
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
	
	public String getPreferedStreamMethod() {
		if(this.streamMethods.contains("http://jabber.org/protocol/bytestreams")) {
			return "http://jabber.org/protocol/bytestreams";
		}else if(this.streamMethods.contains("http://jabber.org/protocol/ibb")){
			return "http://jabber.org/protocol/ibb";
		}
		return "";
	}

	@Override
	public String toString() {
		return "XMPPFileInfo [id=" + id + ", to=" + to + ", name=" + name
				+ ", size=" + size + ", desc=" + desc + ", date=" + date
				+ ", hash=" + hash + ", streamMethods=" + streamMethods + "]";
	}
	
}
