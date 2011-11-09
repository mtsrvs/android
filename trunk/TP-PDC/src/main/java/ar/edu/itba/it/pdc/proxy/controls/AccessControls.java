package ar.edu.itba.it.pdc.proxy.controls;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.config.TimeRange;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.exception.UserSilencedException;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPClientMessageProcessor;

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
	
	public void range(String userInfo) throws InvalidRangeException {
		TimeRange timeRange = this.configLoader.getTimeRanges().get(userInfo);
		
		if (timeRange != null && !timeRange.isInRange(DateTime.now()))
			throw new InvalidRangeException(userInfo + " is not allowed to login at this time.");
	}
	
	public void logins(String userInfo) throws MaxLoginsAllowedException {
		String str = this.configLoader.getLoginsBlacklist().get(userInfo);
		DateTime today = DateTime.now();
		if (this.lastChecked != null
				&& (today.getYear() != this.lastChecked.getYear()
				|| today.getMonthOfYear() != this.lastChecked.getMonthOfYear()
				|| today.getDayOfYear() != this.lastChecked.getDayOfYear()))
			this.currentLogins.clear();
		
		int logins = this.currentLogins.get(userInfo) == null ? 1 : this.currentLogins.get(userInfo) + 1;
		this.currentLogins.put(userInfo, logins); 		
		if (str != null)
			if (Integer.valueOf(str) < logins)
				throw new MaxLoginsAllowedException("Too many logins today for " + userInfo + ".");
			
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
	
	public List<XMPPClientMessageProcessor> concurrentSessions(XMPPClientMessageProcessor cmp){
		Integer maxConcurrentSessions = this.configLoader.getCaccess().get(cmp.getJid().getUsername());
		
		List<XMPPClientMessageProcessor> ans = null;
		if (cmp.getJid().getUsername() != null){
			this.concurrentSessions.add(cmp);
			int concurrentSessions = this.concurrentSessions.queueSize(cmp.getJid().getUsername());			
			
			if (maxConcurrentSessions != null){
				if (maxConcurrentSessions < concurrentSessions--){
					ans = new ArrayList<XMPPClientMessageProcessor>();
					ans.add(this.concurrentSessions.poll(cmp.getJid().getUsername()));
				}
				while(maxConcurrentSessions < concurrentSessions--)
					ans.add(this.concurrentSessions.poll(cmp.getJid().getUsername()));

				Isecu.log.debug("Concurrent session map: " + this.concurrentSessions);
				return ans;
			}
		}
		Isecu.log.debug("Concurrent session map: " + this.concurrentSessions);
		return ans;
	}
	
	public InetSocketAddress multiplex(String username){
		return this.configLoader.getMultiplex().get(username);
		
	}
	
	public void reorder(XMPPClientMessageProcessor cmp){
		this.concurrentSessions.reorder(cmp);
	}
	
	public void remove(XMPPClientMessageProcessor cmp){
		this.concurrentSessions.remove(cmp);
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
		
		public void add(XMPPClientMessageProcessor cmp){
			PriorityQueue<XMPPClientMessageProcessor> q = getQueue(cmp.getJid().getUsername());
			if (q != null)
				q.add(cmp);
			else {
				q = new PriorityQueue<XMPPClientMessageProcessor>(10, reverseOrderComparator());
				q.add(cmp);
				this.sessions.put(cmp.getJid().getUsername(), q);
			}
		}
		
		public void remove(XMPPClientMessageProcessor cmp){
			if (cmp.getJid() != null && cmp.getJid().getResource() != null){
				PriorityQueue<XMPPClientMessageProcessor> q = getQueue(cmp.getJid().getUsername());
				if (q != null){
					q.remove(cmp);
					if (q.isEmpty())
						this.sessions.remove(cmp.getJid().getUsername());
				}
			}
		}
		
		public void reorder(XMPPClientMessageProcessor cmp){
			if (cmp.getJid() != null && cmp.getJid().getUsername() != null){
				PriorityQueue<XMPPClientMessageProcessor> q = getQueue(cmp.getJid().getUsername());
				if (q != null)
					if (q.remove(cmp))
						q.add(cmp);
			}
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
