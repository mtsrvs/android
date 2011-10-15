package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.ConfigurationFileException;

/**
 * Cargador de parámetros de configuración
 */
@Component
public class ConfigLoader {

	private String initConfigPath = "init.properties";
	private Properties initConfig = new Properties();

	private String fullConfigPath = "configuration.properties";
	private Properties config = new Properties();
	
	ConfigLoader(){
		this.updateConfig();
		this.updateInitConfig();
	}

	/**
	 * Carga los parámetros iniciales de la aplicación
	 * @throws ConfigurationFileException
	 */
	public void updateInitConfig() throws ConfigurationFileException {
		try {
			this.initConfig.load(ClassLoader.getSystemResourceAsStream(this.initConfigPath));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationFileException("Init configuration file");
		}
	}
	
	/**
	 * Devuelve una InetSocketAddress donde debe bindearse el proxy
	 * @return
	 * @throws ConfigurationFileException
	 */
	public SocketAddress getProxyAddress() throws ConfigurationFileException {
		try {
			return getAddressProperty("proxy", initConfig);
		} catch (Exception e) {
			throw new ConfigurationFileException("Invalid proxy address");
		} 
	}
	
	/**
	 * Devuelve una InetSocketAddress del server origin default
	 * @return
	 * @throws ConfigurationFileException
	 */
	public SocketAddress getOriginServer() throws ConfigurationFileException {
		try {
			return getAddressProperty("origin", initConfig);
		} catch (Exception e) {
			throw new ConfigurationFileException("Invalid origin address");
		} 
	}
	
	/**
	 * Devuelve una InetSocketAddress donde debe bindearse el servicio de configuración
	 * @return
	 * @throws ConfigurationFileException
	 */
	public SocketAddress getConfigAddress() throws ConfigurationFileException {
		try {
			return getAddressProperty("config", initConfig);
		} catch (Exception e) {
			throw new ConfigurationFileException("Invalid config address");
		} 
	}
		
	/**
	 * Carga la configuración del proxy
	 * @throws ConfigurationFileException
	 */
	public void updateConfig() throws ConfigurationFileException {
		try{
			this.config.load(ClassLoader.getSystemResourceAsStream(this.fullConfigPath));
		} catch (Exception e) {
			throw new ConfigurationFileException("Configuration file");
		}
	}
	
	/**
	 * Tamaño de buffer de lectura y escritura
	 * @return int - tamaño del buffer a utilizar
	 */
	public int getBufferSize() {
		return getIntegerProperty("bufferSize", this.initConfig);
	}
	
	/**
	 * Retorna una direccíon de socket basandose en el archivo de properties
	 * En el archivo necesita el nombre(name) y name + "Port".
	 * @param name
	 * @param prop
	 * @return
	 * @throws UnknownHostException
	 */
	private SocketAddress getAddressProperty(String name, Properties prop) throws UnknownHostException {
		return new InetSocketAddress(InetAddress.getByName(prop.getProperty(name)), getIntegerProperty(name + "Port", prop));
	}
	
	private int getIntegerProperty(String name, Properties prop) {
		return Integer.valueOf(prop.getProperty(name));
	}
}
