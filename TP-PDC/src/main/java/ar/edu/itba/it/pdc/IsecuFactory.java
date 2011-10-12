package ar.edu.itba.it.pdc;

import java.net.InetSocketAddress;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.protocol.ProtocolUtils;

public class IsecuFactory {

	private static IsecuFactory instance;
	
	private ProtocolUtils pu;
	private ConfigLoader cl;
	
	
	public static IsecuFactory getInstance() throws ConfigurationFileException {
		if(instance == null) {
			instance = new IsecuFactory();
		}
		return instance;
	}
	
	private IsecuFactory() throws ConfigurationFileException {
		this.cl = new ConfigLoader("init.properties", "configuration.properties");
		this.pu = new ProtocolUtils(((InetSocketAddress) cl.getProxyAddress()).getPort(), ((InetSocketAddress) cl.getConfigAddress()).getPort());
	}
	
	public ProtocolUtils getProtocolUtils() {
		return pu;
	}
	
	public ConfigLoader getConfigLoader() {
		return cl;
	}
	
}
