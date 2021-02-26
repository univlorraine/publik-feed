package fr.univlorraine.publikfeed.ldap.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * Entité Ldap : People
 */
@SuppressWarnings("serial")
@Data
public class PeopleLdap implements Serializable {

	private String[] objectClass;
	private String eduPersonPrincipalName;
	private String businessCategory;
	private String displayName;
	private String givenName;
	private String sn;
	private String mail;
	private String supannEmpId;
	private String supannEtuId;
	private String supannCivilite;
	private String uid;
	private String[] udlCategories;
	private String modifyTimestamp;


}
