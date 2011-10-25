package ar.edu.itba.it.pdc.proxy.parser;

import org.springframework.stereotype.Component;

import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

@Component
public class ReaderFactory {
	
	private InputFactoryImpl readerFactory = new InputFactoryImpl();

	public AsyncXMLStreamReader newAsyncReader() {
		return readerFactory.createAsyncXMLStreamReader();
	}

}
