package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.WFCException;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public class MessageProcessor {

	private AsyncXMLStreamReader asyncReader;
	private int consumed;
	private int processed;
	private int toWrite;
	private int lastEvent;
	private StringBuilder buffer;

	public MessageProcessor() {
		this.consumed = this.processed = this.toWrite = this.lastEvent = 0;
		this.buffer = new StringBuilder();
		InputFactoryImpl factory = new InputFactoryImpl();
		this.asyncReader = factory.createAsyncXMLStreamReader();
	}
	
	public void read(ByteBuffer bb) {
		bb.flip();
		buffer.append(new String(bb.array()));
		bb.clear();
		process();
	}
	
	public void write(ByteBuffer bb) {
		
		byte data[] = buffer.substring(0, this.toWrite).getBytes();
		
		System.out.println(data.length + "b proccessed");
		
		int i = 0;
		while(bb.position() < bb.limit() && i < this.toWrite) {
			bb.put(data[i++]);
		}
		
		this.buffer = new StringBuilder(this.buffer.substring(i, this.buffer.length()));
		this.consumed += i;
		
		//Lo dejo listo para leer
		bb.flip();
	}

	private void markToWrite(int vLocation) {
		this.toWrite = normalizeLocation(vLocation);
	}
	
	private AsyncInputFeeder getFeeder() {
		return asyncReader.getInputFeeder();
	}

	private void process() {
		try {
			if (getFeeder().needMoreInput()) {
				byte[] data = buffer.substring(processed, buffer.length()).getBytes();
				System.out.println("Feed: " + data.length);
				getFeeder().feedInput(data, 0, data.length);
				this.processed += data.length;
			}

			int event; 
			while ((event = asyncReader.next()) != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				switch(event) {
				case AsyncXMLStreamReader.START_DOCUMENT:
					handleStartDocument(getCurrentLocation());break;
				case AsyncXMLStreamReader.START_ELEMENT:
					handleStartElement(getCurrentLocation());break;
				case AsyncXMLStreamReader.ATTRIBUTE:
					handleAttribute(getCurrentLocation());break;
				default:
					markLastEvent(getCurrentLocation());
				}
			}
		} catch (WFCException e){
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	private int getCurrentLocation() {
		return asyncReader.getLocation().getCharacterOffset();
	}
	
	public String getEventString(int vLocation) {
		return this.buffer.substring(this.lastEvent, normalizeLocation(vLocation));
	}
	
	protected void markLastEvent(int vLocation) {
		this.lastEvent = normalizeLocation(vLocation);
	}
	
	private void sendEvent(int vLocation) {
		markLastEvent(vLocation);
		markToWrite(vLocation);
	}
	
	public void handleStartDocument(int vLocation) {
		System.out.println("StartDoc: " + getEventString(vLocation));
		sendEvent(vLocation);
	}
	
	public void handleStartElement(int vLocation) {
		System.out.println("StartElem: " + getEventString(vLocation));
		sendEvent(vLocation);
	}
	
	public void handleAttribute(int vLocation) {
		System.out.println("Attribute: " + getEventString(vLocation));
		sendEvent(vLocation);
	}
	
	public boolean needToWrite() {
		return this.toWrite != 0;
	}
	
	private int normalizeLocation(int location) {
		return location - this.consumed;
	}

}
