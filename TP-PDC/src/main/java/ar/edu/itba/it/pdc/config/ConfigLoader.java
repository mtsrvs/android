package ar.edu.itba.it.pdc.config;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
		try {
			//TODO porque me pone las barras en el .properties?
			FileOutputStream fos = new FileOutputStream("src/main/resources/" + fullConfigPath);
			config.store(fos, null);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationFileException("Full configuration file");
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
	 * @return List<String> - Lista de IPs en la lista negra.
	 */
	public List<String> getIPBlacklist(){
		return getBlacklistProperty("ipBlacklist", this.config);
	}
	
	/**
	 * @return List<String> - Lista de redes en la lista negra.
	 */
	public List<String> getNetworkBlacklist(){
		return getBlacklistProperty("networkBlacklist", this.config);
	}
	
	/**
	 * @return List<TimeRange> - Lista de restricciones horarias por usuario.
	 */
	public Hashtable<String,TimeRange> getTimeRanges(){
		return getTimeRangesProperty("timeRanges", this.config);
	}
	
	/**
	 * @return	true - Filtro de transformación de texto a L33t activado.
	 * 			false - Caso contrario.
	 */
	public boolean getL33t(){
		return getFilterProperty("l33t", this.config);
	}
	
	/**
	 * @return	true - Filtro de verificación de hash activado.
	 * 			false - Caso contrario.
	 */
	public boolean getHash(){
		return getFilterProperty("hash", this.config);
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
	
	private List<String> getBlacklistProperty(String name, Properties prop) {
		String blacklist = prop.getProperty(name);
		String divisor = ",";
		Pattern pattern = Pattern.compile(divisor);
		String[] items = pattern.split(blacklist);
		return Arrays.asList(items);
	}
	
	private Hashtable<String,TimeRange> getTimeRangesProperty(String name, Properties prop){
		String timeRanges = prop.getProperty(name);
		String divisor = ",";
		Pattern pattern = Pattern.compile(divisor);
		String[] items = pattern.split(timeRanges);
		Hashtable<String,TimeRange> ans = new Hashtable<String,TimeRange>();
		
		for (String s : items){
			StringTokenizer st = new StringTokenizer(s);
			String username = st.nextToken("=");
			int fromH = Integer.valueOf(st.nextToken(":").substring(1));
			int fromM = Integer.valueOf(st.nextToken(":"));
			int fromS = Integer.valueOf(st.nextToken("-").substring(1));
			int toH = Integer.valueOf(st.nextToken(":").substring(1));
			int toM = Integer.valueOf(st.nextToken(":"));
			int toS = Integer.valueOf(st.nextToken());
			ans.put(username, new TimeRange(fromH, fromM, fromS, toH, toM, toS));
		}
		
		return ans;
	}
	
	private boolean getFilterProperty(String name, Properties prop){
		String filter = prop.getProperty(name);
		if (filter.equalsIgnoreCase("on"))
			return true;
		return false;
	}
	
	public void setServer(String origin, String port) {
		initConfig.setProperty("origin", origin);
		initConfig.setProperty("originPort", port);
		try {
			//TODO porque me pone las barras en el .properties?
			FileOutputStream fos = new FileOutputStream("src/main/resources/" + initConfigPath);
			initConfig.store(fos, null);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationFileException("Init configuration file");
		}
	}
}
