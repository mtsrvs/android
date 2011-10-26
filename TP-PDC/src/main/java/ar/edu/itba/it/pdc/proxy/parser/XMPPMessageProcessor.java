package ar.edu.itba.it.pdc.proxy.parser;

import java.nio.ByteBuffer;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.proxy.filters.L33tFilter;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.WFCException;

public abstract class XMPPMessageProcessor {

	private StringBuilder buffer;
	private ConfigLoader configLoader;
	private ReaderFactory readerFactory;
	private AsyncXMLStreamReader asyncReader;
	private int consumed;
	private int processed;
	private int toWrite;
	private int lastEvent;
	private boolean reset = false;
	
	// Access controls/filters variables
	private int total = 0;
	private boolean iqFlag = false;
	private boolean queryFlag = false;
	private boolean usernameFlag = false;
	private boolean messageFlag = false;
	private boolean bodyFlag = false;
	
	protected static enum TagType {
		MESSAGE, SUCCESS, OTHER;
	}

	public XMPPMessageProcessor(ConfigLoader configLoader, ReaderFactory readerFactory) {
		this.consumed = this.processed = this.toWrite = this.lastEvent = 0;
		this.buffer = new StringBuilder();
		this.readerFactory = readerFactory;
		this.asyncReader = this.readerFactory.newAsyncReader();
		
		this.configLoader = configLoader;
	}

	public void read(ByteBuffer bb, int read, String name) {
		if(this.reset) {
			tryToReset();
		}else{
			bb.rewind();
			int i;
			for (i = 0; i < read; i++) {
				buffer.append((char) bb.get());
			}
			System.out.println("Read[" + name + "][" + i + "]");
			
			if(i > 0){
				process();
			}
			
			bb.clear();
		}
	}

	public void write(ByteBuffer bb, String name) {

		byte data[] = this.buffer.substring(0, this.toWrite).getBytes();

		int i = 0;
		for( ; i < data.length && bb.position() < bb.limit(); i++) {
			bb.put(data[i]);
		}
		
		this.buffer = new StringBuilder(this.buffer.substring(i, this.buffer.length()));		
		System.out.println("Write[" + name + "][" + i + "][" + this.buffer.length() + "]");
		
		this.consumed += i;

		this.toWrite -= i;
		this.processed -= i;

		if(this.reset && this.toWrite == 0) {
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
			byte[] data;
			if (getFeeder().needMoreInput()) {				
				data = buffer.substring(processed, buffer.length()).getBytes();
				getFeeder().feedInput(data, 0, data.length);
				this.total += this.buffer.length() - this.processed;
				this.processed += data.length;
			} else {
				throw new IllegalStateException("No need input");
			}
			
			if(this.reset) {
				tryToReset();
			} else {
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
					case AsyncXMLStreamReader.CHARACTERS:
						handleAttribute(getCurrentLocation());
						Location location = getReader().getLocation();
						int position = location.getCharacterOffset();
										
						/*// Prints de testeo
						System.out.println("no tChars: " + getReader().getText());
						System.out.println("buffer: " + this.buffer.toString());
						System.out.println("cant: " + this.cant);
						System.out.println("pos: " + position);	
						System.out.println("buffer size: " + this.buffer.length());*/
						
						if(this.configLoader.getL33t() && this.bodyFlag){
							String bodyText = getReader().getText();
							
							/*// Prints de testeo
							System.out.println(this.buffer.length() - this.cant + position - 1);
							System.out.println(this.buffer.charAt(this.buffer.length() - this.cant + position - 1));
							System.out.println(this.buffer.length() - this.cant + position - 1 + str.length());*/
							
							int start = this.buffer.length() - this.total + position - 1;
							int end = start + bodyText.length();

							this.buffer.replace(start, end, L33tFilter.transform(bodyText));
						}
						//System.out.println("buffer despues: " + this.buffer.toString());
						
						if (this.usernameFlag)
							System.out.println(getReader().getText());
						
						
						markLastEvent(getCurrentLocation());
						break;
					default:
						handleAnyOtherEvent(getCurrentLocation());
						markLastEvent(getCurrentLocation());
					}
				}
			}
		} catch (WFCException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	private void tryToReset() {
		System.out.println("Intenta");
		if(this.toWrite == 0) {
			this.resetReader();
		}
	}
	
	private int getCurrentLocation() {
		return asyncReader.getLocation().getCharacterOffset();
	}

	public String getEventString(int vLocation) {
		return this.buffer.substring(normalizeLocation(this.lastEvent),
				normalizeLocation(vLocation));
	}

	private void markLastEvent(int vLocation) {
		this.lastEvent = vLocation;
	}

	protected void sendEvent(int vLocation) {
		this.toWrite = normalizeLocation(vLocation);
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
	
	public boolean hasResetMessage() {
		return false;
	}
	
	public boolean needToReset() {
		return this.reset;
	}
	
	private void resetReader() {
		System.out.println("Resetea!");
		this.buffer = new StringBuilder(this.buffer.substring(normalizeLocation(getCurrentLocation())));
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

	protected void handleStartDocument(int vLocation) {
		sendEvent(vLocation);
	}
	
	protected void handleStartElement(int vLocation) {
		if (getReader().getName().getLocalPart().equalsIgnoreCase("message"))
			this.messageFlag = true;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("body"))
			this.bodyFlag = this.messageFlag;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("iq"))
			this.iqFlag = true;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("query"))
			this.queryFlag = this.iqFlag;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("username"))
			this.usernameFlag = this.queryFlag;
		
		sendEvent(vLocation);
	}

	protected void handleEndElement(int vLocation) {
		if (getReader().getName().getLocalPart().equalsIgnoreCase("message"))
			this.messageFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("body"))
			this.bodyFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("iq"))
			this.iqFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("query"))
			this.queryFlag = false;
		else if (getReader().getName().getLocalPart().equalsIgnoreCase("username"))
			this.usernameFlag = false;
		
		sendEvent(vLocation);
	}

	protected void handleAttribute(int vLocation) {
		sendEvent(vLocation);
	}

	protected void handleAnyOtherEvent(int vLocation) {
		sendEvent(vLocation);
	}

}
