package ar.edu.itba.it.pdc.proxy.info;

import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPClientMessageProcessor;
import ar.edu.itba.it.pdc.proxy.parser.processor.XMPPServerMessageProcessor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Mapa que mantiene la relación bidireccional entre instancias client/server
 * de XMPPMessageProcessor
 *
 */
@Component
public class XMPPProcessorMap {

	private BiMap<XMPPServerMessageProcessor, XMPPClientMessageProcessor> processors = HashBiMap.create();
	
	public void put(XMPPServerMessageProcessor server, XMPPClientMessageProcessor client) {
		this.processors.put(server, client);
	}
	
	public XMPPClientMessageProcessor getXMPPClientProcessor(XMPPServerMessageProcessor server) {
		return this.processors.get(server);
	}
	
	public XMPPServerMessageProcessor getXMPPServerProcessor(XMPPClientMessageProcessor client) {
		return this.processors.inverse().get(client);
	}
	
}
