package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPClientMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPMessageProcessor;

public abstract class XMPPHandler implements TCPHandler {

	protected static enum Opt {
		READ, WRITE;
	}
	
	private int read = 0;
	private int write = 0;
	
	public void read(SelectionKey key, SelectionKey endPointKey)
			throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		XMPPMessageProcessor processor = getProcessor(key, Opt.READ);
		ByteBuffer buf = this.getReadBuffer(key);

		int r = 0;
		if(!processor.needToReset()) {
			r =  sc.read(buf);
		}
		
		this.read += r;
		
		if(r < 0) {
			Isecu.log.debug("2) I am " + processor.getJid());
			if (processor.isClientProcessor())
				processor.getAccessControls().remove((XMPPClientMessageProcessor)processor);
			closePair(key, endPointKey, "Connection closed by client.");
			return;
		}

		try {
			if(processor.read(buf,r) == r) {
				buf.clear();
			}else{
				buf.compact();
			}
		}catch(Exception e) {
			closePair(key, endPointKey, "Invalid protocol");
			return;
		}
		
		//Se avisa al otro processor que debe reiniciar
		if(processor.hasResetMessage()) {
			getProcessor(key, Opt.WRITE).markToReset();
		}
		
		//Si el proxy tiene que escribir mensajes al cliente
		if(getProcessor(key, Opt.WRITE).needToWrite()) {
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		
		if(processor.needToWrite()) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		
	}
	
	private void closePair(SelectionKey k, SelectionKey ke, String reason) {
		Isecu.log.info("Connection close[" + reason + "]. (Sent Bytes: " + this.read + " Received Bytes: " + this.write + ")");
		SocketChannel sc = (SocketChannel) k.channel();
		try {
			sc.close();
			k.channel().close();
			ke.channel().close();
			k.cancel();
			ke.cancel();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = this.getWriteBuffer(key);
		
		XMPPMessageProcessor processor = getProcessor(key, Opt.WRITE);
		
		buf = processor.write(getWriteBuffer(key));
		this.setWriteBuffer(key, buf);
		if(buf != null) {
			this.write += sc.write(buf);
			if(!buf.hasRemaining()) {
				this.setWriteBuffer(key, null);
			}
		}
		if(!processor.needToWrite()) {
			key.interestOps(SelectionKey.OP_READ);
		}

	}

	protected abstract XMPPMessageProcessor getProcessor(SelectionKey key, Opt opt);
	
	protected abstract ByteBuffer getReadBuffer(SelectionKey key);
	
	protected abstract ByteBuffer getWriteBuffer(SelectionKey key);
	
	protected abstract void setWriteBuffer(SelectionKey key, ByteBuffer wb);
	
	protected abstract String getName();
	
	protected ChannelAttach getAttach(SelectionKey key) {
		return (ChannelAttach) key.attachment();
	}
	
}
