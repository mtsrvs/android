package ar.edu.itba.it.pdc;

import java.io.IOException;

import ar.edu.itba.it.pdc.proxy.XMPPProxy;

public class Isecu {
	
	//TODO CABLEADO PUERTO DEL PROXY Y CANTIDAD DE SELECTORES
	private static final int PORT = 9999;
	private static final int SELECTORS = 1;
	
	private Isecu instance;
	private static final int cores = Runtime.getRuntime().availableProcessors();
	
	
	public static void main(String[] args) throws IOException{
		new XMPPProxy(PORT, SELECTORS).start();
	}
	
	public Isecu getInstance(){
		return instance;
	}

}
