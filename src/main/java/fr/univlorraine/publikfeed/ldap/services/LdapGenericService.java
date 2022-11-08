/**
 *
 * Copyright (c) 2022 Université de Lorraine, 18/02/2021
 *
 * dn-sied-dev@univ-lorraine.fr
 *
 * Ce logiciel est un programme informatique servant à alimenter Publik depuis des groupes LDAP.
 *
 * Ce logiciel est régi par la licence CeCILL 2.1 soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL 2.1, et que vous en avez accepté les
 * termes.
 *
 */
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
