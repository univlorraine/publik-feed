package fr.univlorraine.publikfeed.ldap.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * Entité Ldap : Structure
 */
@SuppressWarnings("serial")
@Data
public class StructureLdap implements Serializable {

	private String[] objectClass;
	private String udlLibelleAffichage;
	private String supannCodeEntite;
	private String modifyTimestamp;


}
