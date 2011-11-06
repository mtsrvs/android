package ar.edu.itba.it.pdc.proxy.controls;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.config.TimeRange;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;

@Component
public class AccessControls {
	
	private ConfigLoader configLoader;
	private Map<String,Integer> currentLogins;
	private DateTime lastChecked;
	
	@Autowired
	public AccessControls(ConfigLoader configLoader){
		this.configLoader = configLoader;
		this.currentLogins = new HashMap<String,Integer>();
	}
	
	public void ip(InetAddress iaddr) throws AccessControlException {
		if (this.configLoader.getIPBlacklist().contains(iaddr)){
			throw new AccessControlException("IP blacklisted!");
		}
	}
	
	public void network(InetAddress iaddr) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		
		for(SubnetUtils net: this.configLoader.getNetworkBlacklist())
			if (net.getInfo().isInRange(addr))
				throw new AccessControlException("Network blacklisted!");
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
}
