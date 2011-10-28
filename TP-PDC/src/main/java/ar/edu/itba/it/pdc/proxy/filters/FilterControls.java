package ar.edu.itba.it.pdc.proxy.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.config.ConfigLoader;

@Component
public class FilterControls {

	private static final String ON = "on";
	
	private ConfigLoader configLoader;
	
	@Autowired
	public FilterControls(ConfigLoader configLoader){
		this.configLoader = configLoader;
	}
	
	public boolean l33t(String username){
		return this.configLoader.getLeet().get(username).equalsIgnoreCase(ON);
	}
	
	public boolean hash(String username){
		return this.configLoader.getHash().get(username).equalsIgnoreCase(ON);
	}
}
