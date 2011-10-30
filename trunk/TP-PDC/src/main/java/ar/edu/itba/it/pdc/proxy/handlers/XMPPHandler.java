package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.parser.XMPPMessageProcessor;

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
			closePair(key, endPointKey);
			return;
		}

		try {
			processor.read(buf,r,getName());
		}catch(Exception e) {
			closePair(key, endPointKey);
			return;
		}
		
		if(processor.hasResetMessage()) {
			getProcessor(key, Opt.WRITE).markToReset();
		}
		
		if(processor.needToWrite()) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		
	}
	
	private void closePair(SelectionKey k, SelectionKey ke) {
		Isecu.log.info("Connection close. (Sent Bytes: " + this.read + " Received Bytes: " + this.write + ")");
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
		
		buf = processor.write(getWriteBuffer(key), getName());
		this.setWriteBuffer(key, buf);
		this.write += sc.write(buf);

		
		if(processor.needToWrite() || buf.hasRemaining()) {
			if(buf.hasRemaining()) {
				buf.compact();
			}else{
				this.setWriteBuffer(key, null);
			}
		}else{
			buf.clear();
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
