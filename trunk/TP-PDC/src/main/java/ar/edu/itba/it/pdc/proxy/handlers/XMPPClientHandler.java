package ar.edu.itba.it.pdc.proxy.handlers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class XMPPClientHandler  {

	private static final int READ = SelectionKey.OP_READ;
	private static final int WRITE = SelectionKey.OP_WRITE;
	
	private InetAddress server;
	private int port;
	private int bufferSize;
	
	public XMPPClientHandler(InetAddress server, int port, int bufferSize){
		this.server = server;
		this.port = port;
		this.bufferSize = bufferSize;
	}

	public void accept(SelectionKey key) throws IOException {
		SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
		SocketChannel serverChannel = SocketChannel.open();
		serverChannel.connect(new InetSocketAddress(server, port));
		
		System.out.println("|||Aceptando nueva conexión|||");
		System.out.println("Cliente:" + clientChannel.socket().getInetAddress().getHostName() + ":" + clientChannel.socket().getPort());
		System.out.println("Server:" + serverChannel.socket().getInetAddress().getHostName() + ":" + serverChannel.socket().getPort());

		clientChannel.configureBlocking(false);
		serverChannel.configureBlocking(false);
		
		clientChannel.register(key.selector(), READ , new ChannelToolkit(bufferSize, serverChannel));
		serverChannel.register(key.selector(), READ , new ChannelToolkit(bufferSize, clientChannel));
		
		System.out.println("|||Conexión aceptada|||");
	}

	public void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ChannelToolkit toolkit = (ChannelToolkit) key.attachment();
		ByteBuffer buffer = toolkit.getBuffer();
		
		buffer.clear();
		long bytesRead = channel.read(buffer);

		System.out.println(new String(buffer.array()));
		
		// Cerró la conexión el otro extremo?
		if (bytesRead == -1){
			channel.close();
			key.cancel();
			return;
		} else if (bytesRead > 0)
			key.interestOps( READ | WRITE );
		
		
		//TODO //// TEMPORAL ///// ALGO ASI COMO EL WORKER THREAD ES ESTO
		SocketChannel endpointChannel = toolkit.getEndpoint();
		SelectionKey endpointKey = endpointChannel.keyFor(key.selector());
		ChannelToolkit newToolkit = (ChannelToolkit)(endpointKey.attachment());
		newToolkit.setBuffer(buffer);
		write(endpointKey);
		
	}

	public void write(SelectionKey key) throws IOException {
		ChannelToolkit toolkit = (ChannelToolkit) key.attachment();
		ByteBuffer buffer = toolkit.getBuffer();
		buffer.flip();

		SocketChannel channel = (SocketChannel) key.channel();
		channel.write(buffer);

		// Buffer lleno?
		if (!buffer.hasRemaining())
			key.interestOps( READ );

		buffer.compact();
	}
}
