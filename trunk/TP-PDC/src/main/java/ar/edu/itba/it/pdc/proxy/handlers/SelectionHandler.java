package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface SelectionHandler {
	
	/**
	 * Se encarga de aceptar la nueva conexión que representa key.
	 * key está disponible para ser aceptada.
	 * @param key
	 * @throws IOException
	 */
    void accept(SelectionKey key) throws IOException;
    
    /**
     * Se encarga de leer/recibir datos de la conexión representada por key.
     * key está disponible para leer/recibir.
     * @param key
     * @throws IOException
     */
    void read(SelectionKey key) throws IOException;
    
    /**
     * Se encarga de escribir/enviar datos de la conexión representada por key.
     * key está disponible para escribir/enviar y es válida.
     * @param key
     * @throws IOException
     */
    void write(SelectionKey key) throws IOException;
}
