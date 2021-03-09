package fr.univlorraine.publikfeed.ldap.exceptions;


@SuppressWarnings("serial")
public class StructureException extends LdapServiceException {


	public StructureException(){
	}
	public StructureException(String message) {
		super(message);
	}
	public StructureException(Throwable cause) {
		super(cause);
	}
	public StructureException(String message, Throwable cause) {
		super(message, cause);
	}
}
