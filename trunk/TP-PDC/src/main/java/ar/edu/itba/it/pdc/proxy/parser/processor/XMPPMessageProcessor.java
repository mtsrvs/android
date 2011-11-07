package ar.edu.itba.it.pdc.proxy.parser.processor;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.exception.InvalidProtocolException;
import ar.edu.itba.it.pdc.exception.InvalidRangeException;
import ar.edu.itba.it.pdc.exception.MaxLoginsAllowedException;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.element.IQStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.MessageStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.PresenceStanza;
import ar.edu.itba.it.pdc.proxy.parser.element.SimpleElement;
import ar.edu.itba.it.pdc.proxy.parser.element.StartElement;
import ar.edu.itba.it.pdc.proxy.parser.element.XMPPElement;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils;
import ar.edu.itba.it.pdc.proxy.parser.element.util.ElemUtils.StanzaType;
import ar.edu.itba.it.pdc.proxy.parser.element.util.StreamConstructor;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public abstract class XMPPMessageProcessor implements XMPPFilter {

	// Parser variables
	protected ConfigLoader configLoader;
	private ReaderFactory readerFactory;
	protected FilterControls filterControls;
	protected AccessControls accessControls;
	private AsyncXMLStreamReader asyncReader;
	private StreamConstructor sc;
	private Queue<XMPPElement> buffer = new LinkedList<XMPPElement>();

	protected XMPPMessageProcessor endpoint;
	protected JID jid = null;

	private boolean reset = false;

	public XMPPMessageProcessor(ConfigLoader configLoader,
								ReaderFactory readerFactory,
								FilterControls filterControls,
								AccessControls accessControls) {
		this.configLoader = configLoader;
		this.readerFactory = readerFactory;
		this.filterControls = filterControls;
		this.accessControls = accessControls;
		this.asyncReader = this.readerFactory.newAsyncReader();
		this.sc = new StreamConstructor(this.filterControls);
	}
	
	public void setEndpoint(XMPPServerMessageProcessor endpoint){
		this.endpoint = endpoint;
	}
	
	public void setEndpoint(XMPPClientMessageProcessor endpoint){
		this.endpoint = endpoint;
	}
	
	protected void appendOnEndpointBuffer(XMPPElement e){
		this.endpoint.buffer.add(e);
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
			XMPPElement ew;
			do {
				ew = buffer.poll();
			}while(ew != null && !ew.needSend());
			if(ew != null) {
				ew.setToWrite();
				tryToReset();
				return ew.getByteBuffer();
			}
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
						processXMPPElement((StartElement) aux);
					}
					break;
				case AsyncXMLStreamReader.END_ELEMENT:
					if ((aux = sc.handleEndElement(asyncReader)) != null) {
						buffer.add(aux);
						processXMPPElement((SimpleElement) aux);
					}
					break;
				case AsyncXMLStreamReader.CHARACTERS:
					if ((aux = sc.handleCharacters(jid, asyncReader, this.isClientProcessor())) != null) {
						buffer.add(aux);
					}
					break;
				default:
					sc.handleOtherEvent(asyncReader);
				}
			}
		} catch (InvalidRangeException e) {
			appendOnEndpointBuffer(sc.handleAccessControlException(e));
		} catch (MaxLoginsAllowedException e){
			buffer.clear();
			buffer.add(sc.handleAccessControlException(e));
		} catch (Exception e) {
			Isecu.log.debug("Invalid Protocol", e);
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
		this.asyncReader = this.readerFactory.newAsyncReader();
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
	 * @throws AccessControlException 
	 */
	private void processXMPPElement(SimpleElement e) throws AccessControlException {
		if(ElemUtils.isStanzaType(e, StanzaType.IQ)){
			handleIqStanza((IQStanza) e);
		} else if(ElemUtils.isStanzaType(e, StanzaType.MESSAGE)){
			handleMessageStanza((MessageStanza) e);
		} else if(ElemUtils.isStanzaType(e, StanzaType.PRESENCE)){
			handlePresenceStanza((PresenceStanza) e);
		} else if (ElemUtils.isElement(e, "response")){
			handleResponseElement(e);
		} else {
			handleOtherElement(e);
		}
	}
	
	protected abstract void processXMPPElement(StartElement e);

	
	public boolean isClientProcessor(){
		return false;
	}
	
	public boolean isServerProcessor(){
		return false;
	}
	
	protected void handleResponseElement(SimpleElement e) throws AccessControlException {
		
	}
}
