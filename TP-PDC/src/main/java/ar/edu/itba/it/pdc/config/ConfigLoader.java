package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.ConfigurationFileException;

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
	
	public String getInitConfigPath() {
		return initConfigPath;
	}

	public void setInitConfigPath(String initConfigPath) {
		this.initConfigPath = initConfigPath;
	}

	public String getFullConfigPath() {
		return fullConfigPath;
	}

	public void setFullConfigPath(String configPath) {
		this.fullConfigPath = configPath;
	}



//	/**
//	 * Carga las configuraciones del proxy y del sistema de los arhivois especificados
//	 * @param initConfigPath Path al archivo de parámetros iniciales.
//	 * @param fullConfigPath Path al archivo de configuración del proxy.
//	 * @throws ConfigurationFileException
//	 */
//	public ConfigLoader(String initConfigPath, String fullConfigPath) throws ConfigurationFileException {
//		this.initConfigPath = initConfigPath;
//		this.updateInitConfig();
//		this.fullConfigPath = fullConfigPath;
//		this.updateConfig();
//	}
	
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
			return new InetSocketAddress(InetAddress.getByName(this.initConfig.getProperty("proxy")),Integer.valueOf(this.initConfig.getProperty("proxyPort")));
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
			return new InetSocketAddress(InetAddress.getByName(this.initConfig.getProperty("origin")),Integer.valueOf(this.initConfig.getProperty("originPort")));
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
			return new InetSocketAddress(InetAddress.getByName(this.initConfig.getProperty("config")),Integer.valueOf(this.initConfig.getProperty("configPort")));
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
	
}
