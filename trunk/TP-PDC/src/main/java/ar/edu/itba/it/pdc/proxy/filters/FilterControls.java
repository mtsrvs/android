package ar.edu.itba.it.pdc.proxy.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

@Component
public class FilterControls {

	private static final String ON = "on";
	
	private ConfigLoader configLoader;
	
	@Autowired
	public FilterControls(ConfigLoader configLoader){
		this.configLoader = configLoader;
	}
	
	public boolean l33t(JID jid){
		if (jid == null)
			return false;
		String l33t = this.configLoader.getLeet().get(jid.getUsername());
		return l33t != null && l33t.equalsIgnoreCase(ON);
	}
	
	public boolean hash(JID jid){
		if (jid == null)
			return false;
		String hash = this.configLoader.getHash().get(jid.getUsername());
		return hash != null && hash.equalsIgnoreCase(ON);
	}
}
