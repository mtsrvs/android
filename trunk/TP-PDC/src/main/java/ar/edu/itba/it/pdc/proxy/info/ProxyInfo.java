package ar.edu.itba.it.pdc.proxy.info;

import java.net.SocketAddress;

/**
 * Información básica del servicio
 */
public class ProxyInfo {

	public ProxyInfo(SocketAddress proxy, SocketAddress config, SocketAddress origin) {
		this.proxy = proxy;
		this.config = config;
		this.origin = origin;
	}
	
	private SocketAddress proxy;
	private SocketAddress config;
	private SocketAddress origin;

	public SocketAddress getProxy() {
		return proxy;
	}
	public SocketAddress getConfig() {
		return config;
	}
	public SocketAddress getOrigin() {
		return origin;
	}
	
}
