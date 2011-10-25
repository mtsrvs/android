package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

/**
 * Cargador de parámetros de configuración
 */
@Component
public class ConfigUtils{

	public boolean ipIsValid(String ip) {
		if(ip != "") {
			try {
				InetAddress.getByName(ip);
				return true;
			} catch (UnknownHostException e) {
				return false;
			}
		}
		return false;
	}
	
}
