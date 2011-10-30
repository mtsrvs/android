package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.XMPPProcessorMap;
import ar.edu.itba.it.pdc.proxy.protocol.JID;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.WFCException;

public abstract class XMPPMessageProcessor {

	// Parser variables
	private ReaderFactory readerFactory;
	protected FilterControls filterControls;
	private AsyncXMLStreamReader asyncReader;

	protected XMPPProcessorMap xmppProcessorMap;
	
	protected JID jid;
	protected MessageBuffer messageBuffer = new MessageBuffer();

	protected boolean digestMD5Flag = false;
	protected boolean nonSASLFlag = false;
	
	private boolean reset = false;

	private boolean iqFlag = false; // TODO TODAVIA NO ESTA EN USO
	private boolean queryFlag = false; // TODO TODAVIA NO ESTA EN USO
	private boolean usernameFlag = false; // TODO TODAVIA NO ESTA EN USO
	private boolean jidFlag = false; // TODO TODAVIA NO ESTA EN USO
	private boolean messageFlag = false;
	private boolean bodyFlag = false;

	protected static enum TagType {
		MESSAGE, BODY, IQ, SUCCESS, QUERY, JID, AUTH, USERNAME, RESOURCE, STREAM, OTHER;
	}

	public XMPPMessageProcessor(ReaderFactory readerFactory, FilterControls filterControls, XMPPProcessorMap xmppProcessorMap) {
		this.readerFactory = readerFactory;
		this.filterControls = filterControls;
		this.asyncReader = this.readerFactory.newAsyncReader();
		this.jid = null;
		this.xmppProcessorMap = xmppProcessorMap;
	}

	/**
	 * Appendea al buffer
	 * 
	 * @param bb
	 *            ByteBuffer de donde lee
	 * @param read
	 *            Cantidad de bytes a appendear
	 * @param name
	 *            Nombre para imprimir mensajes
	 */
	public void read(ByteBuffer bb, int read, String name) {
		if (this.reset) {
			tryToReset();
		} else {
			bb.flip();

			if (messageBuffer.append(bb) > 0) {
				process();
			}
			bb.clear();
		}
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
	public ByteBuffer write(ByteBuffer byteBuffer, String name) {
		ByteBuffer ret = this.messageBuffer.write(byteBuffer);
		if (this.reset && !this.messageBuffer.hasToWrite()) {
			this.resetReader();
		}
		return ret;
	}

	/**
	 * Devuelve el feeder que utiliza el processor.
	 * @return
	 */
	private AsyncInputFeeder getFeeder() {
		return asyncReader.getInputFeeder();
	}

	/**
	 * Procesa los últimos ingresados al buffer y maneja los eventos XML
	 * correspondientes.
	 */
	private void process() {
		try {
			byte[] data;
			if (getFeeder().needMoreInput()) {
				data = this.messageBuffer.getNotProcessedData();
				getFeeder().feedInput(data, 0, data.length);
			} else {
				throw new IllegalStateException("No need input");
			}

			if (this.reset) {
				tryToReset();
			} else {
				int event;
				while ((event = asyncReader.next()) != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
					/*System.out.println("EVENTO: " + event);
					System.out.println(AsyncXMLStreamReader.START_DOCUMENT);
					System.out.println(AsyncXMLStreamReader.END_DOCUMENT);
					System.out.println(AsyncXMLStreamReader.START_ELEMENT);
					System.out.println(AsyncXMLStreamReader.END_ELEMENT);
					System.out.println(AsyncXMLStreamReader.ATTRIBUTE);
					System.out.println(AsyncXMLStreamReader.CHARACTERS);*/
					switch (event) {
					case AsyncXMLStreamReader.START_DOCUMENT:
						handleStartDocument(getCurrentLocation());
						this.messageBuffer.markLastEvent(getCurrentLocation());
						break;
					case AsyncXMLStreamReader.START_ELEMENT:
						handleStartElement(getCurrentLocation());						
						this.messageBuffer.markLastEvent(getCurrentLocation());
						break;
					case AsyncXMLStreamReader.END_ELEMENT:
						handleEndElement(getCurrentLocation());
						this.messageBuffer.markLastEvent(getCurrentLocation());
						break;
					case AsyncXMLStreamReader.ATTRIBUTE:
						handleAttribute(getCurrentLocation());
						this.messageBuffer.markLastEvent(getCurrentLocation());
						break;
					case AsyncXMLStreamReader.CHARACTERS:
						handleAttribute(getCurrentLocation());
						Location location = getReader().getLocation();
						int position = location.getCharacterOffset();

						/*
						 * // Prints de testeo System.out.println("no tChars: "
						 * + getReader().getText());
						 * System.out.println("buffer: " +
						 * this.buffer.toString()); System.out.println("cant: "
						 * + this.cant); System.out.println("pos: " + position);
						 * System.out.println("buffer size: " +
						 * this.buffer.length());
						 */

						// TODO ESTA CABLEADO A USER
//						if (this.bodyFlag && this.filterControls.l33t("user")) {
//							String bodyText = getReader().getText();

							/*
							 * // Prints de testeo
							 * System.out.println(this.buffer.length() -
							 * this.cant + position - 1);
							 * System.out.println(this
							 * .buffer.charAt(this.buffer.length() - this.cant +
							 * position - 1));
							 * System.out.println(this.buffer.length() -
							 * this.cant + position - 1 + str.length());
							 */

							// int start = this.buffer.length() - this.total +
							// position - 1;
							// int end = start + bodyText.length();
							//
							// this.buffer.replace(start, end,
							// L33tFilter.transform(bodyText));
//						}
						// System.out.println("buffer despues: " +
						// this.buffer.toString());

						if (this.usernameFlag)
							System.out.println(getReader().getText());
						if (this.jidFlag)
							System.out.println(getReader().getText());

						handleCharacters(getCurrentLocation());
						this.messageBuffer.markLastEvent(getCurrentLocation());
						break;
					default:
						handleAnyOtherEvent(getCurrentLocation());
						this.messageBuffer.markLastEvent(getCurrentLocation());
					}
				}
			}
		} catch (WFCException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Resetea el processor, si es que todavía no hay datos marcados para
	 * escribir.
	 */
	private void tryToReset() {
		if (!this.messageBuffer.hasToWrite()) {
			this.resetReader();
		}
	}

	/**
	 * Posición actual del XMLReader
	 * 
	 * @return
	 */
	private int getCurrentLocation() {
		return asyncReader.getLocation().getCharacterOffset();
	}

	/**
	 * Indica si el processor tiene datos para escribir.
	 * 
	 * @return true/false
	 */
	public boolean needToWrite() {
		return this.messageBuffer.hasToWrite();
	}

	/**
	 * Marca que es necesario resetear el processor.
	 */
	public void markToReset() {
		this.reset = true;
	}

	/**
	 * Indica si hay mensajes de reset.
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
	 * Resetea el processor
	 */
	private void resetReader() {
		this.messageBuffer.reset(getCurrentLocation());
		this.asyncReader = readerFactory.newAsyncReader();
		this.reset = false;
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
	 * Indica el tipo de elemento XML.
	 * 
	 * @param localPart
	 * @return
	 */
	protected TagType getTagType(String localPart) {
		if (localPart.equalsIgnoreCase("message")) {
			return TagType.MESSAGE;
		} else if (localPart.equalsIgnoreCase("success")) {
			return TagType.SUCCESS;
		} else if (localPart.equalsIgnoreCase("iq")) {
			return TagType.IQ;
		} else if (localPart.equalsIgnoreCase("jid")) {
			return TagType.JID;
		} else if (localPart.equalsIgnoreCase("body")) {
			return TagType.BODY;
		} else if (localPart.equalsIgnoreCase("query")) {
			return TagType.QUERY;
		} else if (localPart.equalsIgnoreCase("auth")) {
			return TagType.AUTH;
		} else if (localPart.equalsIgnoreCase("username")) {
			return TagType.USERNAME;
		} else if (localPart.equalsIgnoreCase("resource")) {
			return TagType.RESOURCE;
		} else if (localPart.equalsIgnoreCase("stream")) {
			return TagType.STREAM;
		} else {
			return TagType.OTHER;
		}
	}

	/**
	 * Maneja el evento de comienzo de Stream XML. Ejemplo: <?xml version="1.0"
	 * encoding="UTF-8"?>
	 * 
	 * @param vLocation
	 */
	protected void handleStartDocument(int vLocation) {
		this.messageBuffer.setCharset(Charset.forName(this.asyncReader
				.getEncoding()));
		this.messageBuffer.markEventToSend(vLocation);
	}

	/**
	 * Maneja el evento de comienzo de una tag.
	 * 
	 * @param vLocation
	 */
	protected void handleStartElement(int vLocation) {
		if (getReader().getName().getLocalPart().equalsIgnoreCase("message"))
			this.messageFlag = true;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("body"))
			this.bodyFlag = this.messageFlag;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("iq"))
			this.iqFlag = true;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("query"))
			this.queryFlag = this.iqFlag;
		else if (getReader().getName().getLocalPart()
				.equalsIgnoreCase("username"))
			this.usernameFlag = this.queryFlag;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("jid"))
			this.jidFlag = true;

		this.messageBuffer.markEventToSend(vLocation);
	}

	/**
	 * Menaje el evento de finalización de una tag.
	 * 
	 * @param vLocation
	 */
	protected void handleEndElement(int vLocation) {
		if (getReader().getName().getLocalPart().equalsIgnoreCase("message"))
			this.messageFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("body"))
			this.bodyFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("iq"))
			this.iqFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("query"))
			this.queryFlag = false;
		else if (getReader().getName().getLocalPart()
				.equalsIgnoreCase("username"))
			this.usernameFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("jid"))
			this.jidFlag = false;

		this.messageBuffer.markEventToSend(vLocation);
	}

	/**
	 * Maneja el evento de un atributo XML.
	 * 
	 * @param vLocation
	 */
	protected void handleAttribute(int vLocation) {
		this.messageBuffer.markEventToSend(vLocation);
	}
	
	/**
	 * Maneja un evento orientado a caracteres.
	 * @param vLocation
	 */
	protected void handleCharacters(int vLocation) {
		this.messageBuffer.markEventToSend(vLocation);
	}

	/**
	 * Maneja cualquier otro evento.
	 * 
	 * @param vLocation
	 */
	protected void handleAnyOtherEvent(int vLocation) {
		this.messageBuffer.markEventToSend(vLocation);
	}
	
}
