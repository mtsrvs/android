package ar.edu.itba.it.pdc;

import java.io.IOException;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.ConfigurationFileException;
import ar.edu.itba.it.pdc.proxy.IsecuServer;

public class Isecu {
	
	private Isecu instance;
	
	public static void main(String[] args) throws IOException{
		try {
			ConfigLoader cl = IsecuFactory.getInstance().getConfigLoader();
			new IsecuServer(cl.getProxyAddress(), cl.getConfigAddress(), cl.getOriginServer()).start();
		}catch (ConfigurationFileException e) {
			e.printStackTrace();
			System.out.println("No se puede arrancar el proxy. Error en la configuración.");
		}
	}
	
	public Isecu getInstance(){
		return instance;
	}

}
