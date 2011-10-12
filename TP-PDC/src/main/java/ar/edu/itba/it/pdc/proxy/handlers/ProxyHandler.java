package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.proxy.IsecuServer;

public abstract class ProxyHandler implements TCPProxyHandler, Runnable {

	protected SelectionKey key;
	protected SocketChannel sEndPoint;
	protected int action;
	
	public ProxyHandler(final SelectionKey key, final SocketChannel sEndPoint, final int action) {
		this.key = key;
		this.sEndPoint = sEndPoint;
		this.action = action;
	}
	
	public void run() {
		try{
			switch(action) {
			case IsecuServer.READ:
				this.read(key, sEndPoint);break;
			case IsecuServer.WRITE:
				this.write(key);
			}
		}catch(IOException e) {
			e.printStackTrace();
			System.out.println("Problemas en el ProxyHandler");
		}
		
		//Se vuelve a poner de interés la acción que se atendía
		if(key.isValid()) {
			key.interestOps(key.interestOps() | action);
		}
		
	}

	public abstract void read(SelectionKey key, SocketChannel sEndPoint) throws IOException;

	public abstract void write(SelectionKey key) throws IOException;
	
	
}
