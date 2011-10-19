package ar.edu.itba.it.pdc.proxy.handlers;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class HandlerUtils {

	public static SelectionKey getKey(SocketChannel sc, Selector sel) {
		return sc.keyFor(sel);
	}
	
}
