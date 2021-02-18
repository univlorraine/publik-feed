package fr.univlorraine.publikfeed.ldap.exceptions;

@SuppressWarnings("serial")
public class LdapServiceException extends Exception {


	public LdapServiceException(){
	}
	public LdapServiceException(String message) {
		super(message);
	}
	public LdapServiceException(Throwable cause) {
		super(cause);
	}
	public LdapServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
