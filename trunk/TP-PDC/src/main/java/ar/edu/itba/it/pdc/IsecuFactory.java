package ar.edu.itba.it.pdc;

import java.net.InetAddress;

import javax.xml.parsers.FactoryConfigurationError;

import ar.edu.itba.it.pdc.exception.UninitiatedFactoryException;
import ar.edu.itba.it.pdc.proxy.protocol.ProtocolUtils;

public class IsecuFactory {

	private static IsecuFactory instance;
	
	private ProtocolUtils pu;
	
	public static void init(InetAddress origin, int originPort, InetAddress proxy, int proxyPort, int configPort) {
		instance = new IsecuFactory(new ProtocolUtils(proxyPort, configPort));
	}
	
	public static IsecuFactory getInstance() {
		if(instance == null) {
			throw new UninitiatedFactoryException("Factory no iniciado");
		}
		return instance;
	}
	
	private IsecuFactory(ProtocolUtils pu) {
		this.pu = pu;
	}
	
	public ProtocolUtils getProtocolUtils() {
		return pu;
	}
	
}
