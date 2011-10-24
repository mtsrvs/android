package ar.edu.itba.it.pdc.proxy.controls;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.DateTime;

import ar.edu.itba.it.pdc.config.TimeRange;
import ar.edu.itba.it.pdc.exception.AccessControlException;

public class AccessControls {
	
	public static void ip(InetAddress iaddr, List<String> blacklist) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		if (blacklist.contains(addr))
			throw new AccessControlException("IP blacklisted!");
	}
	
	public static void network(InetAddress iaddr, List<String> blacklist) throws AccessControlException {
		String addr = iaddr.getHostAddress();
		
		for(String net: blacklist)
			if (new SubnetUtils(net).getInfo().isInRange(addr))
				throw new AccessControlException("Network blacklisted!");
	}
	
	public static void range(String username, Hashtable<String,TimeRange> timeRanges) throws AccessControlException {
		TimeRange timeRange = timeRanges.get(username);
		
		if (timeRange != null && !timeRange.isInRange(DateTime.now()))
			throw new AccessControlException("You are not allowed to login at this time.");
	}
}
