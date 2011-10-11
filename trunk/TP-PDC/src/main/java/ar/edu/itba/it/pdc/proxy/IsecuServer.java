package ar.edu.itba.it.pdc.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import ar.edu.itba.it.pdc.IsecuFactory;
import ar.edu.itba.it.pdc.proxy.handlers.ProxyClientHandler;
import ar.edu.itba.it.pdc.proxy.handlers.ProxyServerHandler;
import ar.edu.itba.it.pdc.proxy.map.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.protocol.Protocol;

public class IsecuServer {
	
	public static final int BUFFER_SIZE = 512;
	
	public static final int READ = SelectionKey.OP_READ;
	public static final int WRITE = SelectionKey.OP_WRITE;
	public static final int ACCEPT = SelectionKey.OP_ACCEPT;
	
	private SocketAddress proxy;
	private SocketAddress config;
	private SocketAddress origin;
	
	private ConnectionMap connMap = new ConnectionMap();;
	
	/**
	 * Inicializa el proxy
	 * @param origin Address del servidor origin default.
	 * @param originPort Puerto del servidor origin default.
	 * @param proxy Interfaz donde se bindea el servidor proxy.
	 * @param proxyPort Puerto donde se bindea el servidor proxy.
	 */
	public IsecuServer(InetAddress origin, int originPort, InetAddress proxy, int proxyPort, int configPort){
		this.proxy = new InetSocketAddress(proxy, proxyPort);
		this.config = new InetSocketAddress(proxy, configPort);
		this.origin = new InetSocketAddress(origin, originPort);
	}
	
	
	/**
	 * Inicia el servidor proxy
	 * @throws IOException
	 */
	public void start() throws IOException {
		//Falta largar el ServerSocket del servicio de configuración
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(proxy);
		serverChannel.register(selector, ACCEPT);
	
		while(true){
			
			//Esto lo hago porque los threads cambian las interestOps, por ende
			//tiene que ir actualizando. Preguntarle a Juan. TODO
			if (selector.select(100) == 0) {
                continue;
            }
			
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                if (key.isValid()){
                	
	                if (key.isAcceptable()) {
	                	this.handleAccept(key);
	                }
	                
	                if (key.isReadable()) {
	                	this.handleReadWrite(key, READ);
	                }
	                
	                if (key.isWritable()) {
	                	this.handleReadWrite(key, WRITE);
	                }
	                
	                iterator.remove();
                }
            }
		}
	}
	
	/**
	 * Lanza thread para atender lectura/escritura dependiendo de la conexión
	 * @param key
	 */
	private void handleReadWrite(SelectionKey key, int action) {
		Protocol p = IsecuFactory.getInstance().getProtocolUtils().expectedProtocol(key);
		
		//Se desactiva la acción mientras se atiende
		key.interestOps(key.interestOps() ^ action);
		
		if(p == Protocol.CLIENT) {
			SocketChannel endPoint = (action == READ) ? connMap.getServerChannel(key.channel()) : null;
			//Usar pool de threds
			new Thread(new ProxyClientHandler(key, endPoint, action)).start();
		}else if(p == Protocol.CONFIG) {
			System.out.println("Debería manejar Config!");
			//TODO: Thread que maneje la config
		}else{
			SocketChannel endPoint = (action == READ) ? connMap.getClientChannel(key.channel()) : null;
			//User pool de threds
			new Thread(new ProxyServerHandler(key, endPoint, action)).start();
		}
		
	}
	
	/**
	 * Acepta conexiones
	 * @param key
	 */
	private void handleAccept(SelectionKey key) {
		Protocol p = IsecuFactory.getInstance().getProtocolUtils().expectedProtocol(key);

		if(p == Protocol.CLIENT) {
			connectClient(key);
		}else{
			connectConfig(key);
		}
	}
	
	/**
	 * Realiza la conexión al origin server y registra el par de conexiones
	 * @param key
	 */
	private void connectClient(SelectionKey key) {
		SocketChannel ss;
		try {
			SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
			//Acá debería hacerse la multiplexación de usuarios
			ss = SocketChannel.open(origin);
			ss.configureBlocking(false);
			connMap.addConnection(sc, ss);
			sc.configureBlocking(false);
			sc.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(BUFFER_SIZE));
			ss.register(key.selector(), SelectionKey.OP_READ, new ChannelAttach(BUFFER_SIZE));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Falla accept de client");
		}
	}
	
	/**
	 * Registra la conexión de administración
	 * @param key
	 */
	private void connectConfig(SelectionKey key) {
		try {
			SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
			sc.configureBlocking(false);
			sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUFFER_SIZE));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Falla accept de config");
		}
	}
}
