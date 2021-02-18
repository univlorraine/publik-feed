package fr.univlorraine.publikfeed.model.ldap.entity;

import java.io.Serializable;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import lombok.Getter;

/**
 * Ldap Person.
 * @author Adrien Colson
 */
@Entry(objectClasses = { "person", "top" }, base = "ou=people")
@Getter
@SuppressWarnings("serial")
public final class LdapPerson implements Serializable {

	@Id
	private Name id;

	@Attribute
	private String uid;

	@Attribute
	private String displayName;

}
