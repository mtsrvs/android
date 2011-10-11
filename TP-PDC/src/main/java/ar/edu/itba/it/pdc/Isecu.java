package ar.edu.itba.it.pdc;

import java.io.IOException;
import java.net.InetAddress;

import ar.edu.itba.it.pdc.proxy.IsecuServer;

public class Isecu {
	
	private Isecu instance;
	
	public static void main(String[] args) throws IOException{
		byte[] ipProxy = {0,0,0,0};
		int proxyPort = 9999;
		int configPort = 9998;
		int portServer = 5222;
		InetAddress interfazProxy = InetAddress.getByAddress(ipProxy);
		InetAddress interfazServer = InetAddress.getLocalHost();
		IsecuFactory.init(interfazServer, portServer, interfazProxy, proxyPort, configPort);
		new IsecuServer(interfazServer, portServer, interfazProxy, proxyPort, configPort).start();
	}
	
	public Isecu getInstance(){
		return instance;
	}

}
