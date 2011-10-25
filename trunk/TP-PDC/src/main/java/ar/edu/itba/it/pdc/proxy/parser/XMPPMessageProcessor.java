package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLStreamException;

import ar.edu.itba.it.pdc.proxy.handlers.ReaderFactory;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.WFCException;

public abstract class XMPPMessageProcessor {

	private ReaderFactory readerFactory;
	private AsyncXMLStreamReader asyncReader;
	private int consumed;
	private int processed;
	private int toWrite;
	private int lastEvent;
	private StringBuilder buffer;
	
	private boolean reset = false;

	protected static enum TagType {
		MESSAGE, SUCCESS, OTHER;
	}

	public XMPPMessageProcessor(ReaderFactory readerFactory) {
		this.consumed = this.processed = this.toWrite = this.lastEvent = 0;
		this.buffer = new StringBuilder();
		this.readerFactory = readerFactory;
		this.asyncReader = this.readerFactory.newAsyncReader();
	}

	public void read(ByteBuffer bb, int read) {
		if(!this.reset) {
			bb.rewind();
			int i;
			for (i = 0; i < read; i++) {
				buffer.append((char) bb.get());
			}
			bb.clear();
			process();
		}
	}

	public void write(ByteBuffer bb) {

		byte data[] = buffer.substring(0, this.toWrite).getBytes();

		int i = 0;
		while (bb.position() < bb.limit() && i < this.toWrite) {
			bb.put(data[i++]);
		}

		this.buffer = new StringBuilder(this.buffer.substring(i,
				this.buffer.length()));

		System.out.println("Se escriben " + i + " para salida");

		this.consumed += i;

		this.toWrite -= i;
		this.processed -= i;

		if(this.toWrite == 0) {
			this.resetReader();
		}
		
		// Lo dejo listo para leer
		bb.flip();
	}

	private AsyncInputFeeder getFeeder() {
		return asyncReader.getInputFeeder();
	}

	private void process() {
		try {
			if (getFeeder().needMoreInput()) {
				byte[] data = buffer.substring(processed, buffer.length())
						.getBytes();
				getFeeder().feedInput(data, 0, data.length);
				this.processed += data.length;
			} else {
				throw new IllegalStateException("No need input");
			}

			int event;
			while ((event = asyncReader.next()) != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				switch (event) {
				case AsyncXMLStreamReader.START_DOCUMENT:
					handleStartDocument(getCurrentLocation());
					markLastEvent(getCurrentLocation());
					break;
				case AsyncXMLStreamReader.START_ELEMENT:
					handleStartElement(getCurrentLocation());
					markLastEvent(getCurrentLocation());
					break;
				case AsyncXMLStreamReader.END_ELEMENT:
					handleEndElement(getCurrentLocation());
					break;
				case AsyncXMLStreamReader.ATTRIBUTE:
					handleAttribute(getCurrentLocation());
					markLastEvent(getCurrentLocation());
					break;
				default:
					handleAnyOtherEvent(getCurrentLocation());
				}

			}
		} catch (WFCException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	private int getCurrentLocation() {
		return asyncReader.getLocation().getCharacterOffset();
	}

	public String getEventString(int vLocation) {
		return this.buffer.substring(normalizeLocation(this.lastEvent),
				normalizeLocation(vLocation));
	}

	private void markToWrite(int vLocation) {
		this.toWrite = normalizeLocation(vLocation);
	}

	protected void markLastEvent(int vLocation) {
		this.lastEvent = vLocation;
	}

	protected void sendEvent(int vLocation) {
		markLastEvent(vLocation);
		markToWrite(vLocation);
	}

	public boolean needToWrite() {
		return this.toWrite != 0;
	}

	private int normalizeLocation(int location) {
		return location - this.consumed;
	}

	public void markToReset() {
		this.reset = true;
	}
	
	public boolean needToReset() {
		return this.reset;
	}
	
	private void resetReader() {
		this.buffer = new StringBuilder(this.buffer.substring(
				normalizeLocation(getCurrentLocation()), this.buffer.length()));
		this.toWrite = this.processed = this.consumed = 0;
		this.asyncReader = readerFactory.newAsyncReader();
		this.reset = false;
	}

	protected AsyncXMLStreamReader getReader() {
		return this.asyncReader;
	}

	protected TagType getTagType(String localPart) {
		if (localPart.equalsIgnoreCase("message")) {
			return TagType.MESSAGE;
		} else if (localPart.equalsIgnoreCase("success")) {
			return TagType.SUCCESS;
		} else {
			return TagType.OTHER;
		}
	}

	protected abstract void handleStartDocument(int vLocation);

	protected abstract void handleStartElement(int vLocation);

	protected abstract void handleAttribute(int vLocation);

	protected abstract void handleEndElement(int vLocation);

	protected abstract void handleAnyOtherEvent(int vLocation);

}
