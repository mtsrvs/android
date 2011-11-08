package ar.edu.itba.it.pdc.config;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private ConfigLoaderUtils configLoaderUtils;
	
	@Autowired
	ConfigLoader(ConfigLoaderUtils configLoaderUtils){
		this.updateConfig();
		this.updateInitConfig();
		this.configLoaderUtils = configLoaderUtils;
	}

	/**
	 * Carga los parámetros iniciales de la aplicación
	 * @throws ConfigurationFileException
	 */
	public void updateInitConfig() throws ConfigurationFileException {
		try {
			this.initConfig.load(ClassLoader.getSystemResourceAsStream(this.initConfigPath));
		} catch (Exception e) {
			
			throw new ConfigurationFileException("Init configuration file");
		}
	}
	
	/**
	 * Devuelve una property tal cual como esta en el archivo
	 * @param prop
	 * @return
	 */
	public String getProperty(String prop) {
		return config.getProperty(prop);
	}
	
	/**
	 * Setea una property tal cual como se la pasa por parametro
	 * @param prop
	 * @param value
	 */
	public void setProperty(String prop, String value) {
		config.setProperty(prop, value);
	}
	
	public void commit() {
		try {
			FileOutputStream fos = new FileOutputStream(ClassLoader.getSystemResource(fullConfigPath).getPath());
			config.store(fos, null);
			fos.close();
		} catch (Exception e) {
			throw new ConfigurationFileException("Full configuration file");
		}
	}
	
	public void revert() {
		this.updateConfig();
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
	
	public int getFileTransferBufferSize() {
		return getIntegerProperty("fileTrasferBufferSize", this.initConfig);
	}
	
	/**
	 * Lista de IPs en la lista negra.
	 * @return List<String>
	 */
	public List<InetAddress> getIPBlacklist() {
		return configLoaderUtils.getInetAddressList(config.getProperty("ipBlacklist"));
	}
	
	/**
	 * Lista de redes en la lista negra.
	 * @return List<String>
	 */
	public List<SubnetUtils> getNetworkBlacklist() {
		return configLoaderUtils.getNetList(config.getProperty("netBlacklist"));
	}
	
	/**
	 * @return	true - Filtro de transformación de texto a L33t activado.
	 * 			false - Caso contrario.
	 */
	
	/**
	 * Restricciones horarias por usuario.
	 * @return Map<String,TimeRange>
	 */
	public Map<String,TimeRange> getTimeRanges() {
		return configLoaderUtils.getTimeRangesMap(config.getProperty("rangeBlacklist"));
	}
	
	public Map<String,String> getLoginsBlacklist() {
		return configLoaderUtils.getStringStringMap(config.getProperty("loginsBlacklist"));
	}
	
	/**
	 * Filtro leet por usuario
	 * @return Map<String, String> 
	 */
	public Map<String, String> getLeet(){
		return configLoaderUtils.getStringStringMap(config.getProperty("leet"));
	}
	
	/**
	 * Filtro hash por usuario
	 * @return Map<String, String> 
	 */
	public Map<String, String> getHash(){
		return configLoaderUtils.getStringStringMap(config.getProperty("hash"));
	}
	
	/**
	 * Multiplexador de cuentas
	 * @return Map<String, InetAddress>
	 */
	public Map<String, InetAddress> getMultiplex(){
		return configLoaderUtils.getStringInetMap(config.getProperty("multiplex"));
	}
	
	/**
	 * Control de concurrencia 
	 * @return Map<String, Integer>
	 */
	public Map<String, Integer> getCaccess(){
		return configLoaderUtils.getStringIntegerMap(config.getProperty("caccess"));
	}
	
	/**
	 * Lista de usuarios silenciados
	 * @return List<String>
	 */
	public List<String> getSilence(){
		return configLoaderUtils.getStringList(config.getProperty("silence"));
	}
	
	/**
	 * Nombre de usuario del administrador de configuracion
	 * @return
	 */
	public String getAdminUsername() {
		return initConfig.getProperty("adminUsername");
	}
	
	/**
	 * Contraseña del administrador de configuracion
	 * @return
	 */
	public String getAdminPassword() {
		return initConfig.getProperty("adminPassword");
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

	/**
	 * Dado un usuario de la forma user@domain indica si hay que validar o no el hash
	 * @param user
	 * @return
	 */
	public boolean checkHash(String user) {
		String status = getHash().get(user);
		return status == null ? false : status.equalsIgnoreCase("on");
	}
	
}
