package ar.edu.itba.it.pdc.exception;

@SuppressWarnings("serial")
public class CommandValidationException extends RuntimeException {

	public CommandValidationException(){
		super();
	}
	
	public CommandValidationException(String arg0){
		super(arg0);
	}
}
