package ar.edu.itba.it.pdc.exception;

@SuppressWarnings("serial")
public class UninitiatedFactoryException extends RuntimeException {

	public UninitiatedFactoryException() {
		super();
	}

	public UninitiatedFactoryException(String arg0) {
		super(arg0);
	}
	
}
