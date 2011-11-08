package ar.edu.itba.it.pdc.proxy.controls;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.config.TimeRange;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.exception.UserSilencedException;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPClientMessageProcessor;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

@Component
public class AccessControls {
	
	private ConfigLoader configLoader;
	private Map<String,Integer> currentLogins;
	private DateTime lastChecked;
	private ConcurrentSessions concurrentSessions;
	
	@Autowired
	public AccessControls(ConfigLoader configLoader){
		this.configLoader = configLoader;
		this.currentLogins = new HashMap<String,Integer>();
		this.concurrentSessions = new ConcurrentSessions();
	}
	
	public void ip(InetAddress iaddr) throws AccessControlException {
		if (this.configLoader.getIPBlacklist().contains(iaddr)){
			throw new AccessControlException("IP Blocked[" + iaddr.getHostAddress() + "]");
		}
	}
	
	public void network(InetAddress iaddr) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		
		for(SubnetUtils net: this.configLoader.getNetworkBlacklist())
			if (net.getInfo().isInRange(addr))
				throw new AccessControlException("Network Blocked[" + net.getInfo().getAddress() + "]: " + addr);
	}
	
	public void range(String username) throws InvalidRangeException {
		TimeRange timeRange = this.configLoader.getTimeRanges().get(username);
		
		if (timeRange != null && !timeRange.isInRange(DateTime.now()))
			throw new InvalidRangeException(username + " is not allowed to login at this time.");
	}
	
	public void logins(String username) throws MaxLoginsAllowedException {
		String str = this.configLoader.getLoginsBlacklist().get(username);
		DateTime today = DateTime.now();
		if (this.lastChecked != null
				&& (today.getYear() != this.lastChecked.getYear()
				|| today.getMonthOfYear() != this.lastChecked.getMonthOfYear()
				|| today.getDayOfYear() != this.lastChecked.getDayOfYear()))
			this.currentLogins.clear();
		
		int logins = this.currentLogins.get(username) == null ? 1 : this.currentLogins.get(username) + 1;
		this.currentLogins.put(username, logins); 		
		if (str != null)
			if (Integer.valueOf(str) < logins)
				throw new MaxLoginsAllowedException("Too many logins today for " + username + ".");
			
		this.lastChecked = today;		
	}
	
	public void silencerTo(String username) throws UserSilencedException {
		if (this.configLoader.getSilence().contains(username))
			throw new UserSilencedException(username + " is silenced.");
	}
	
	public void silencerFrom(String username) throws UserSilencedException {
		if (this.configLoader.getSilence().contains(username))
			throw new UserSilencedException("You, " + username + ", are silenced.");
	}
	
	public XMPPClientMessageProcessor concurrentSessions(JID jid, XMPPClientMessageProcessor cmp){
		Integer maxConcurrentSessions = this.configLoader.getCaccess().get(jid.getUsername());
		
		System.out.println("maxConcurrentSessions: " + maxConcurrentSessions);
		
		if (maxConcurrentSessions != null && jid.getResource() != null){
			
			System.out.println("map antes: " + this.concurrentSessions);
			System.out.println("concurrentSessions antes: " + concurrentSessions);
			
			this.concurrentSessions.add(jid.getUsername(), cmp);
			int concurrentSessions = this.concurrentSessions.queueSize(jid.getUsername());
			
			System.out.println("concurrentSessions despues: " + concurrentSessions);			
			System.out.println("map despues: " + this.concurrentSessions);
			
			if (maxConcurrentSessions < concurrentSessions)
				return this.concurrentSessions.poll(jid.getUsername());
		}
		return null;
	}
	
	private class ConcurrentSessions {
		private Map<String,PriorityQueue<XMPPClientMessageProcessor>> sessions;
		
		public ConcurrentSessions(){
			this.sessions = new HashMap<String,PriorityQueue<XMPPClientMessageProcessor>>();
		}
		
		public PriorityQueue<XMPPClientMessageProcessor> getQueue(String username){
			return this.sessions.get(username);
		}
		
		public XMPPClientMessageProcessor poll(String username){
			XMPPClientMessageProcessor cmp = getQueue(username).poll();
			if (getQueue(username).isEmpty())
				this.sessions.remove(username);
			return cmp;
		}
		
		public int queueSize(String username){
			PriorityQueue<XMPPClientMessageProcessor> p = this.sessions.get(username);
			return p == null ? 0 : p.size();
		}
		
		public boolean add(String username, XMPPClientMessageProcessor cmp){
			PriorityQueue<XMPPClientMessageProcessor> q = getQueue(username);
			if (q != null)
				q.add(cmp);
			else {
				q = new PriorityQueue<XMPPClientMessageProcessor>(10, reverseOrderComparator());
				q.add(cmp);
				this.sessions.put(username, q);
			}			
			return true;
		}
		
		private Comparator<XMPPClientMessageProcessor> reverseOrderComparator(){
			return new Comparator<XMPPClientMessageProcessor>(){

				public int compare(XMPPClientMessageProcessor arg0,
						XMPPClientMessageProcessor arg1) {
					return arg0.compareTo(arg1);
				}
				
			};
		}
		
		public String toString(){
			return this.sessions.toString();
		}
	}
}
