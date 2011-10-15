package ar.edu.itba.it.pdc.proxy.info;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Mapa que matiene la relación bidireccional entre los sockets.
 */
public class ConnectionMap {

	private BiMap<SocketChannel, SocketChannel> connections = HashBiMap.create();;
	
	public void addConnection(SocketChannel client, SocketChannel server) {
		connections.put(client, server);
	}
	
	public SocketChannel getServerChannel(SelectableChannel client) {
		return connections.get(client);
	}
	
	public SocketChannel getClientChannel(SelectableChannel server) {
		return connections.inverse().get(server);
	}
	
}
