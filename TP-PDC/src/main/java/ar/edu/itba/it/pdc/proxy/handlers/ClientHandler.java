package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.Isecu;
import ar.edu.itba.it.pdc.config.ConfigLoader;
import ar.edu.itba.it.pdc.exception.AccessControlException;
import ar.edu.itba.it.pdc.proxy.ChannelAttach;
import ar.edu.itba.it.pdc.proxy.controls.AccessControls;
import ar.edu.itba.it.pdc.proxy.filetransfer.FileTransferManager;
import ar.edu.itba.it.pdc.proxy.filters.FilterControls;
import ar.edu.itba.it.pdc.proxy.info.ConnectionMap;
import ar.edu.itba.it.pdc.proxy.parser.ReaderFactory;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPMessageProcessor;

/**
 * Manejador de los eventos del socket a cliente.
 */
@Component
public class ClientHandler extends XMPPHandler {
	
	private ConfigLoader configLoader;
	private ConnectionMap connectionMap;
	private ReaderFactory readerFactory;
	private FilterControls filterControls;
	private AccessControls accessControls;
	private FileTransferManager fileManager;
	
	@Autowired
	public ClientHandler(ConfigLoader configLoader, ConnectionMap connectionMap, ReaderFactory readerFactory, FilterControls filterControls, AccessControls accessControls, FileTransferManager fileManager) {
		this.connectionMap = connectionMap;
		this.readerFactory = readerFactory;
		this.filterControls = filterControls;
		this.accessControls = accessControls;
		this.configLoader = configLoader;
		this.fileManager = fileManager;
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel ss;
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		
		try {
			InetAddress iaddr = sc.socket().getInetAddress();
			this.accessControls.ip(iaddr);
			this.accessControls.network(iaddr);
		} catch(AccessControlException e) {
			Isecu.log.info("Access denied: " + e.getMessage());
			sc.close();
			return;
		}
		
		ss = SocketChannel.open(configLoader.getOriginServer());
		ss.configureBlocking(false);
		connectionMap.addConnection(sc, ss);
		sc.configureBlocking(false);
		ChannelAttach attach = new ChannelAttach(this.configLoader, this.readerFactory, this.filterControls, this.accessControls, this.fileManager, this.connectionMap);
		//attach.associateChannelWithProcessor(sc, key.selector(), attach);
		//attach.associateChannelWithProcessor(ss);
		sc.register(key.selector(), SelectionKey.OP_READ, attach);
		ss.register(key.selector(), SelectionKey.OP_READ, attach);
	}

	@Override
	protected XMPPMessageProcessor getProcessor(SelectionKey key, Opt opt) {
		return opt == Opt.READ ? getAttach(key).getClientProcessor() : getAttach(key).getServerProcessor();
 	}

	@Override
	protected ByteBuffer getReadBuffer(SelectionKey key) {
		return getAttach(key).getReadClientBuffer();
	}

	@Override
	protected ByteBuffer getWriteBuffer(SelectionKey key) {
		return getAttach(key).getWriteClientBuffer();
	}

	@Override
	protected String getName() {
		return "Client";
	}

	@Override
	protected void setWriteBuffer(SelectionKey key, ByteBuffer wb) {
		getAttach(key).setWriteClientBuf(wb);
	}

}
