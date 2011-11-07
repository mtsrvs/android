package ar.edu.itba.it.pdc.exception;

@SuppressWarnings("serial")
public class UserSilencedException extends AccessControlException {

	public UserSilencedException(String arg0){
		super(arg0);
	}
}
