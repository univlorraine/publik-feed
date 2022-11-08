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

import java.util.List;

import javax.annotation.Resource;
import javax.naming.Name;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.TimeLimitExceededException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.exceptions.PeopleException;

/** @author Charlie Dubois */
@SuppressWarnings("serial")
@Primary
@Component
public class LdapPeopleServiceImpl implements LdapGenericService<PeopleLdap> {

	/** Logger */
	static final Logger LOG = LoggerFactory.getLogger(LdapPeopleServiceImpl.class);
	
	/** le base DN pour les recherches ldap */
	public static String BASE_DN = "ou=people";

	/** Ldap Template de lecture */
	@Resource
	private LdapTemplate ldapTemplateRead;

	@Override
	public Name buildDn(PeopleLdap o) throws LdapServiceException {
		if (o != null) {
			return buildDn(o.getUid(), "people");
		}
		throw new PeopleException("People null");
	}

	protected Name buildDn(String uid, String ou) {
		
		//LdapName dn = LdapUtils.newLdapName("uid="+uid+", ou="+ou);
		System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, DistinguishedName.KEY_CASE_FOLD_NONE);
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou", ou);
		dn.add("uid", uid);
		return dn;
	}

	@Override
	public void create(PeopleLdap o) throws LdapServiceException {
		DirContextAdapter context = new DirContextAdapter(buildDn(o));
		mapToContext(o, context);
		ldapTemplateRead.bind(context);
	}

	@Override
	public void update(PeopleLdap o) throws LdapServiceException {
		DirContextOperations context = ldapTemplateRead.lookupContext(buildDn(o));
		mapToContext(o, context);
		ldapTemplateRead.modifyAttributes(context);
	}

	@Override
	public void createOrUpdate(PeopleLdap o) throws LdapServiceException {
		PeopleLdap oLdap = findByPrimaryKey(o.getUid());
		if (oLdap != null && StringUtils.hasText(oLdap.getUid())) {
			update(o);
		} else {
			create(o);
		}
	}

	@Override
	public void delete(PeopleLdap o) throws LdapServiceException {
		Name dn = buildDn(o);
		ldapTemplateRead.unbind(dn);
	}

	@Override
	public PeopleLdap findByPrimaryKey(String uid) throws LdapServiceException {
		PeopleLdap r = new PeopleLdap();
		r.setUid(uid);
		try {
			r = (PeopleLdap) ldapTemplateRead.lookup(buildDn(r), getContextMapper());
		} catch (NameNotFoundException e) {
			// TODO contains pas top
			if (e.getMessage().contains("error code 32 - No Such Object")) {
				return null;
			}
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PeopleLdap findEntityByFilter(String filter) throws LdapServiceException {
		PeopleLdap r = new PeopleLdap();
		try {
			List<Object> l = ldapTemplateRead.search(BASE_DN, filter, getContextMapper());
			if (l != null && l.size() > 0) {
				r = (PeopleLdap) l.get(0);
			} else {
				r = null;
			}
		} catch (NameNotFoundException e) {
			// TODO contains pas top
			if (e.getMessage().contains("error code 32 - No Such Object")) {
				return null;
			}
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PeopleLdap> findEntitiesByFilter(String filter) throws LdapServiceException {
		List<PeopleLdap> l = null;
		try {
			String[] attributes = {"objectClass","eduPersonPrincipalName","businessCategory","displayName","givenName","sn","mail","supannEmpId","supannEtuId","supannCivilite","uid","udlCategories","udlFonction","modifyTimestamp"};
			/* Utilisation du ldap de lecture pour nombre de résultat illimité */
			l = ldapTemplateRead.search(BASE_DN, filter, SearchScope.ONELEVEL.getId(), attributes, getContextMapper());
				//search(BASE_DN, filter,SearchScope.ONELEVEL, attributes, getContextMapper());
		} catch (NameNotFoundException e) {
			// TODO contains pas top
			if (e.getMessage().contains("error code 32 - No Such Object")) {
				return null;
			}
		} catch (TimeLimitExceededException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			return null;
		} catch (SizeLimitExceededException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			return null;
		}
		return l;
	}

	@Override
	public void mapToContext(PeopleLdap o, DirContextOperations context) {
		mapAttributsToContext(o, context);
	}

	/** Contrôle que l'attribut n'est pas null ou vide et l'ajoute au context */
	public void addAttributToContext(String attributName, String s, DirContextOperations context) {
		if (StringUtils.hasText(s)) {
			context.setAttributeValue(attributName, s);
		}
	}

	/** Contrôle que l'attribut multivalué n'est pas null et l'ajoute au context */
	public void addAttributToContext(String attributName, String[] s, DirContextOperations context) {
		if (s != null && s.length > 0) {
			context.setAttributeValues(attributName, s);
		}
	}

	public void mapAttributsToContext(PeopleLdap o, DirContextOperations context) {
		addAttributToContext("objectclass", o.getObjectClass(), context);
		addAttributToContext("eduPersonPrincipalName", o.getEduPersonPrincipalName(), context);
		addAttributToContext("businessCategory", o.getBusinessCategory(), context);
		addAttributToContext("displayName", o.getDisplayName(), context);
		addAttributToContext("givenName", o.getGivenName(), context);
		addAttributToContext("sn", o.getSn(), context);
		addAttributToContext("mail", o.getMail(), context);
		addAttributToContext("supannEmpId", o.getSupannEmpId(), context);
		addAttributToContext("supannEtuId", o.getSupannEtuId(), context);
		addAttributToContext("supannCivilite", o.getSupannCivilite(), context);
		addAttributToContext("modifyTimestamp", o.getModifyTimestamp(), context);
		addAttributToContext("uid", o.getUid(), context);
		addAttributToContext("udlCategories", o.getUdlCategories(), context);
		addAttributToContext("udlFonction", o.getUdlFonction(), context);
	}

	@Override
	public ContextMapper getContextMapper() {
		return new PeopleContextMapper();
	}

	private static class PeopleContextMapper extends AbstractContextMapper {
		@Override
		public PeopleLdap doMapFromContext(DirContextOperations context) {
			PeopleLdap o = new PeopleLdap();
			o.setObjectClass(context.getStringAttributes("objectClass"));
			o.setUid(context.getStringAttribute("uid"));
			o.setEduPersonPrincipalName(context.getStringAttribute("eduPersonPrincipalName"));
			o.setBusinessCategory(context.getStringAttribute("businessCategory"));
			o.setDisplayName(context.getStringAttribute("displayName"));
			o.setGivenName(context.getStringAttribute("givenName"));
			o.setSn(context.getStringAttribute("sn"));
			o.setMail(context.getStringAttribute("mail"));
			o.setSupannEmpId(context.getStringAttribute("supannEmpId"));
			o.setSupannEtuId(context.getStringAttribute("supannEtuId"));
			o.setSupannCivilite(context.getStringAttribute("supannCivilite"));
			o.setModifyTimestamp(context.getStringAttribute("modifyTimestamp"));
			o.setUid(context.getStringAttribute("uid"));
			o.setUdlCategories(context.getStringAttributes("udlCategories"));
			o.setUdlFonction(context.getStringAttributes("udlFonction"));
			return o;
		}
	}

}
