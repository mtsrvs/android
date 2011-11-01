package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.InvalidProtocolException;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StreamConstructor;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public abstract class XMPPMessageProcessor {

	// Parser variables
	protected ConfigLoader configLoader;
	private ReaderFactory readerFactory;
	protected FilterControls filterControls;
	private AsyncXMLStreamReader asyncReader;
	private StreamConstructor sc = new StreamConstructor();
	private Queue<XMPPElement> buffer = new LinkedList<XMPPElement>();

	protected XMPPProcessorMap xmppProcessorMap;

	protected JID jid;

	protected boolean digestMD5Flag = false;
	protected boolean nonSASLFlag = false;

	private boolean reset = false;

	protected static enum TagType {
		MESSAGE, BODY, IQ, SUCCESS, QUERY, JID, AUTH, USERNAME, RESOURCE, STREAM, OTHER;
	}

	public XMPPMessageProcessor(ConfigLoader configLoader,
			ReaderFactory readerFactory, FilterControls filterControls,
			XMPPProcessorMap xmppProcessorMap) {
		this.configLoader = configLoader;
		this.readerFactory = readerFactory;
		this.filterControls = filterControls;
		this.asyncReader = this.readerFactory.newAsyncReader();
		this.jid = null;
		this.xmppProcessorMap = xmppProcessorMap;
	}

	
	/**
	 * Lee el ByteBuffer y manda a procesar su contenido
	 * @param bb
	 * @param read
	 * @return
	 */
	public int read(ByteBuffer bb, int read) {
		tryToReset();
		if(!this.reset && read > 0) {
			bb.flip();
			byte[] data = new byte[read];
			bb.get(data, 0, read);
			process(data);
			return read;
		}
		return 0;
	}

	/**
	 * En caso de que el byteBuffer sea nulo, devuelve un ByteBuffer con los
	 * datos listos para escribir; en caso de que se pase un byteBuffer se
	 * añaden la cantidad de bytes que se pueda a este mismo.
	 * 
	 * @param byteBuffer
	 *            null o byteBuffer que se esté utilizando para escribir
	 * @param name
	 * @return
	 */
	public ByteBuffer write(ByteBuffer byteBuffer) {
		tryToReset();
		if (byteBuffer == null && this.needToWrite()) {
			XMPPElement ew = buffer.poll();
			ew.setToWrite();
			tryToReset();
			return ew.getByteBuffer();
		}
		return byteBuffer;
	}

	/**
	 * Devuelve el feeder que utiliza el processor.
	 * 
	 * @return
	 */
	private AsyncInputFeeder getFeeder() {
		return asyncReader.getInputFeeder();
	}

	/**
	 * Procesa los últimos ingresados al buffer y maneja los eventos XML
	 * correspondientes.
	 */
	private void process(byte[] data) {
		try {
			if (getFeeder().needMoreInput()) {
				getFeeder().feedInput(data, 0, data.length);
			} else {
				throw new IllegalStateException("No need input");
			}

			int event;
			while ((event = asyncReader.next()) != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				XMPPElement aux;
				switch (event) {
				case AsyncXMLStreamReader.START_DOCUMENT:
					buffer.add(sc.handleStartDocument(asyncReader));
					break;
				case AsyncXMLStreamReader.START_ELEMENT:
					if ((aux = sc.handleStartElement(asyncReader)) != null) {
						buffer.add(aux);
					}
					break;
				case AsyncXMLStreamReader.END_ELEMENT:
					if ((aux = sc.handleEndElement(asyncReader)) != null) {
						buffer.add(aux);
						processXMPPElement((SimpleElement) aux);
					}
					break;
				case AsyncXMLStreamReader.CHARACTERS:
					if ((aux = sc.handleCharacters(asyncReader)) != null) {
						buffer.add(aux);
					}
					break;
				default:
					sc.handleOtherEvent(asyncReader);
				}
			}
		} catch (Exception e) {
			throw new InvalidProtocolException("Invalid protocol");
		}

	}

	/**
	 * En caso de tener y poder resetear el processor lo hace.
	 */
	private void tryToReset() {
		if (this.reset && this.buffer.size() == 0) {
			this.reset();
		}
	}

	/**
	 * Resetea el processor
	 */
	private void reset() {
		this.reset = false;
		this.asyncReader = readerFactory.newAsyncReader();
		this.sc.reset();
	}

	/**
	 * Marca que es necesario resetear el processor.
	 */
	public void markToReset() {
		this.reset = true;
	}

	/**
	 * Indica si hay mensajes de reset que enviar.
	 * 
	 * @return
	 */
	public boolean hasResetMessage() {
		return false;
	}

	/**
	 * Indica si el processor necesita reseearse.
	 * 
	 * @return
	 */
	public boolean needToReset() {
		return this.reset;
	}

	/**
	 * Indica si el processor tiene datos para escribir.
	 * 
	 * @return true/false
	 */
	public boolean needToWrite() {
		return this.buffer.size() > 0;
	}

	/**
	 * Devuelve el XML Reader.
	 * 
	 * @return
	 */
	protected AsyncXMLStreamReader getReader() {
		return this.asyncReader;
	}

	/**
	 * Procesa el elemento XMPP que se agrega al buffer
	 * 
	 * @param e
	 */
	protected abstract void processXMPPElement(SimpleElement e);

}
