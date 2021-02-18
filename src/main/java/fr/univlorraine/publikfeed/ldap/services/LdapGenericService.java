package fr.univlorraine.publikfeed.ldap.services;

import java.io.Serializable;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;

import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;





/**
 * @author Matthieu MANGINOT
 *
 * @param <T>
 */
public interface LdapGenericService<T> extends Serializable{

	/**
	 * Retourne le DN pour l'objet en paramètre
	 * @param o
	 * @return
	 * @throws LdapServiceException
	 */
	public Name buildDn(T o) throws LdapServiceException;

	/**
	 * Création dans ldap de l'objet
	 * @param o
	 * @throws LdapServiceException
	 */
	public void create(T o) throws LdapServiceException;

	/**
	 * Mise à jour dans ldap de l'objet
	 * @param o
	 * @throws LdapServiceException
	 */
	public void update(T o) throws LdapServiceException;

	/**
	 * Création ou mise à jour dans ldap en
	 * fonction de la présence ou non de l'objet dans ldap
	 * @param o
	 * @throws LdapServiceException
	 */
	public void createOrUpdate(T o) throws LdapServiceException;

	/**
	 * Suppression dans ldap de l'objet
	 * @param o
	 * @throws LdapServiceException 
	 */
	public void delete(T o) throws LdapServiceException;

	/**
	 * Recherche par DN
	 * @param o
	 * @return
	 * @throws LdapServiceException
	 */
	public T findByPrimaryKey(String o) throws LdapServiceException;

	/**
	 * 
	 * @param filter
	 * @return
	 * @throws LdapServiceException
	 */
	public T findEntityByFilter(String filter) throws LdapServiceException;

	/**
	 * 
	 * @param filter
	 * @return
	 * @throws LdapServiceException
	 */
	public List<T> findEntitiesByFilter(String filter) throws LdapServiceException;

	/**
	 * Mapping de l'objet vers l'entité LDAP
	 * @param o
	 * @param context
	 */
	public void mapToContext(T o, DirContextOperations context);

	/**
	 * Mapping l'entité LDAP vers l'objet
	 * @return
	 */
	public ContextMapper getContextMapper();

}
