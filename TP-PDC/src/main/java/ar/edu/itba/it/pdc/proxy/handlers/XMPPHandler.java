package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.parser.XMPPMessageProcessor;

public abstract class XMPPHandler implements TCPHandler {

	protected static enum Opt {
		READ, WRITE;
	}
	
	public void read(SelectionKey key, SelectionKey endPointKey)
			throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		XMPPMessageProcessor processor = getProcessor(key, Opt.READ);
		ByteBuffer buf = this.getReadBuffer(key);

		int r = 0;
		if(!processor.needToReset()) {
			r =  sc.read(buf);
		}
		
		processor.read(buf,r,getName());

		if(r < 0) {
			sc.close();
			key.cancel();
			endPointKey.channel().close();
			endPointKey.cancel();
			return;
		}
		
		if(processor.hasResetMessage()) {
			System.out.println("Se marca el otro para resetear");
			getProcessor(key, Opt.WRITE).markToReset();
		}
		
		if(processor.needToWrite()) {
			endPointKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		
	}

	public void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = this.getWriteBuffer(key);
		
		XMPPMessageProcessor processor = getProcessor(key, Opt.WRITE);
		
		processor.write(getWriteBuffer(key), getName());
		sc.write(buf);

		
		if(processor.needToWrite() || buf.hasRemaining()) {
			if(buf.hasRemaining()) {
				buf.compact();
			}else{
				buf.clear();
			}
			//Sigue prendido el write
		}else{
			buf.clear();
			System.out.println("Stop write " + getName() + "\n");
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	protected abstract XMPPMessageProcessor getProcessor(SelectionKey key, Opt opt);
	
	protected abstract ByteBuffer getReadBuffer(SelectionKey key);
	
	protected abstract ByteBuffer getWriteBuffer(SelectionKey key);
	
	protected abstract String getName();
	
	protected ChannelAttach getAttach(SelectionKey key) {
		return (ChannelAttach) key.attachment();
	}
	
}
