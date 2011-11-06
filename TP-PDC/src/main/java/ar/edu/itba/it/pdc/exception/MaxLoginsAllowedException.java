package ar.edu.itba.it.pdc.exception;

@SuppressWarnings("serial")
public class MaxLoginsAllowedException extends AccessControlException {

	public MaxLoginsAllowedException(String arg0){
		super(arg0);
	}
	
}
