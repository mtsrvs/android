package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MessageBuffer {

	private StringBuilder buffer = new StringBuilder();

	private int consumed = 0;
	private int processed = 0;
	private int lastEvent = 0;
	private int decOffset = 0;
	
	private int toWrite = 0;
	
	private Charset charset = Charset.defaultCharset();

	/**
	 * Charset que se está utilizando
	 * @return
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Asigna el charset
	 * @param charset
	 */
	public void setCharset(Charset charset) {
		System.out.println("Encoding: " + charset.name());
		this.charset = charset;
	}

	/**
	 * Cantidad de datos que ya han sido enviados.
	 * @return
	 */
	public int getConsumed() {
		return consumed;
	}

	/**
	 * Puntero de datos procesados.
	 * @return
	 */
	public int getProcessed() {
		return processed;
	}

	/**
	 * Puntero de datos a escribir.
	 * @return
	 */
	public int getToWrite() {
		return toWrite;
	}

	/**
	 * Ultima posición marcada como evento.
	 * @return
	 */
	public int getLastEvent() {
		return lastEvent;
	}
	
	/**
	 * Decodifica y appendea el buffer los datos del ByteBuffer.
	 * @param data
	 * @return
	 */
	public int append(ByteBuffer data) {
		byte[] b = new byte[data.remaining()];
		data.get(b, 0, b.length);
		return this.append(b);
	}
	
	/**
	 * Appeandea al buffer los datos en el CharBuffer.
	 * @param data
	 * @return
	 */
	private int append(byte[] data) {
		this.buffer.append(new String(data, this.charset));
		return data.length;
	}

	/**
	 * En caso de que el byteBuffer enviado sea nulo, se devuelve un ByteBuffer
	 * wrapeando los datos marcados para escribir.
	 * Caso contrario se meten todos los bytes posibles en el byteBuffer enviado.
	 * @param byteBuffer
	 * @return
	 */
	public ByteBuffer write(ByteBuffer byteBuffer) {
		int w;
		byte[] data = this.buffer.substring(0, Math.min(this.toWrite, this.buffer.length())).getBytes(this.charset);
		System.out.println("Data: " + new String(data, this.charset));
		if(byteBuffer == null) {
			byteBuffer = ByteBuffer.wrap(data);
			w = byteBuffer.limit();
		} else {
			for(w = 0; w < data.length && byteBuffer.position() < byteBuffer.limit(); w++) {
				byteBuffer.put(data[w]);
			}
			byteBuffer.flip();
		}
		
		this.consumed += w;
		this.toWrite -= w;
		this.processed -= w;
		
		this.buffer.delete(0, w);
		return byteBuffer;
	}
	
	/**
	 * Indica si el buffer tiene datos marcados para escribir.
	 * @return
	 */
	public boolean hasToWrite() {
		return this.toWrite > 0;
	}
	
	/**
	 * Devuelve los datos todavía no procesados y los marca como procesados.
	 * @return
	 */
	public byte[] getNotProcessedData() {
		byte[] ret = this.buffer.substring(this.processed, this.buffer.length()).getBytes(this.charset);
		this.processed += ret.length;
		return ret;
	}
	
	/**
	 * Marca la posición del último evento.
	 * @param vLocation
	 */
	public void markLastEvent(int vLocation) {
		this.lastEvent = vLocation;
	}
	
	/**
	 * Marca un evento para ser enviado.
	 * @param vLocation
	 */
	public void markEventToSend(int vLocation) {
		this.toWrite = nl(vLocation);
	}
	
	/**
	 * Devuelve el texto que generó el evento XML.
	 * @param vLocation posición actual
	 * @return String texto del evento
	 */
	public byte[] getEventData(int vLocation) {
		return this.buffer.substring(nl(this.lastEvent), nl(vLocation)).getBytes(this.charset);
	}
	
	/**
	 * Normaliza direcciones al tamaño del buffer.
	 * @param vLocation
	 * @return
	 */
	public int nl(int vLocation) {
		return vLocation - this.consumed - this.decOffset;
	}
	
	/**
	 * Resetea el buffer.
	 * Dejando en el nuevo buffer, los datos todavía no procesados.
	 * @param vLocation
	 */
	public void reset(int vLocation) {
		this.buffer.delete(0, nl(vLocation));
		this.toWrite = this.processed = this.lastEvent = this.consumed = 0;
	}

	public StringBuilder replace(int start, int end, String str) throws StringIndexOutOfBoundsException {
		return this.buffer.replace(start, end, str);
	}
	
	public String toString(){
		return this.buffer.toString();
	}
}
