package ar.edu.itba.it.pdc.proxy.controls;

import java.net.InetAddress;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.config.TimeRange;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

@Component
public class AccessControls {
	
	private ConfigLoader configLoader;
	
	@Autowired
	public AccessControls(ConfigLoader configLoader){
		this.configLoader = configLoader;
	}
	
	public void ip(InetAddress iaddr) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		if (this.configLoader.getIPBlacklist().contains(addr))
			throw new AccessControlException("IP blacklisted!");
	}
	
	public void network(InetAddress iaddr) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		
		for(SubnetUtils net: this.configLoader.getNetworkBlacklist())
			if (net.getInfo().isInRange(addr))
				throw new AccessControlException("Network blacklisted!");
	}
	
	public void range(JID jid) throws AccessControlException {
		TimeRange timeRange = this.configLoader.getTimeRanges().get(jid.getUsername());
		
		if (timeRange != null && !timeRange.isInRange(DateTime.now()))
			throw new AccessControlException("You are not allowed to login at this time.");
	}
}
