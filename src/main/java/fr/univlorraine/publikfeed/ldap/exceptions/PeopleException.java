package fr.univlorraine.publikfeed.ldap.exceptions;


@SuppressWarnings("serial")
public class PeopleException extends LdapServiceException {


	public PeopleException(){
	}
	public PeopleException(String message) {
		super(message);
	}
	public PeopleException(Throwable cause) {
		super(cause);
	}
	public PeopleException(String message, Throwable cause) {
		super(message, cause);
	}
}
