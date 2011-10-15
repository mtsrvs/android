package ar.edu.itba.it.pdc;

import java.net.InetSocketAddress;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.handlers.ClientHandler;
import ar.edu.itba.it.pdc.proxy.handlers.ConfigHandler;
import ar.edu.itba.it.pdc.proxy.handlers.ServerHandler;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.protocol.ProtocolUtils;

public class IsecuFactory {

	private static IsecuFactory instance;
	
	private ProtocolUtils pu;
	private ConfigLoader cl;
	
	private ClientHandler pch;
	private ServerHandler psh;
	private ConfigHandler ch;
	
	private ConnectionMap connectionMap;
	
	public static IsecuFactory getInstance() throws ConfigurationFileException {
		if(instance == null) {
			instance = new IsecuFactory();
		}
		return instance;
	}
	
	private IsecuFactory() throws ConfigurationFileException {
		this.cl = new ConfigLoader("init.properties", "configuration.properties");
		this.pu = new ProtocolUtils(((InetSocketAddress) cl.getProxyAddress()).getPort(), ((InetSocketAddress) cl.getConfigAddress()).getPort());
		
		this.pch = new ClientHandler();
		this.psh = new ServerHandler();
		
		this.connectionMap = new ConnectionMap();
	}
	
	public ProtocolUtils getProtocolUtils() {
		return pu;
	}
	
	public ConfigLoader getConfigLoader() {
		return cl;
	}

	public ClientHandler getClientHandler() {
		return pch;
	}

	public ServerHandler getServerHandler() {
		return psh;
	}
	
	public ConfigHandler getConfigHandler() {
		return ch;
	}

	public ConnectionMap getConnectionMap() {
		return connectionMap;
	}

}
