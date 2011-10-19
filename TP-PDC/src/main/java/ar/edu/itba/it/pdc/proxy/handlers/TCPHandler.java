package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPHandler {
	
    /**
     * Lee del socket seleccionado para leer.
     * @param key 
     * @param endPoint SocketChannel del endPoint. (En caso de no ser necesario es NULL).
     * @throws IOException
     */
    void read(SelectionKey key, SelectionKey endPointKey) throws IOException;
    
    /**
     * Escribe del socket seleccionado para escribir.
     * Escribe lo que tenga en el buffer de escritura.
     * @param key
     * @throws IOException
     */
    void write(SelectionKey key) throws IOException;
    
    /**
     * Acepta la conexión y coloca el socket en el selector
     * @param key
     * @throws IOException
     */
    void accept(SelectionKey key) throws IOException;
}
