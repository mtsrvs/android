package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface TCPProxyHandler {
	
    /**
     * Se encarga de leer/recibir datos de la conexión representada por key.
     * key está disponible para leer/recibir.
     * @param key
     * @throws IOException
     */
    void read(SelectionKey key, SocketChannel endPoint) throws IOException;
    
    /**
     * Se encarga de escribir/enviar datos de la conexión representada por key.
     * key está disponible para escribir/enviar y es válida.
     * @param key
     * @throws IOException
     */
    void write(SelectionKey key) throws IOException;
}
