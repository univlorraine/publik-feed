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

import fr.univlorraine.publikfeed.ldap.entity.StructureLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.exceptions.StructureException;

/** @author Charlie Dubois */
@SuppressWarnings("serial")
@Primary
@Component
public class LdapStructureServiceImpl implements LdapGenericService<StructureLdap> {

	/** Logger */
	static final Logger LOG = LoggerFactory.getLogger(LdapStructureServiceImpl.class);
	
	/** le base DN pour les recherches ldap */
	public static String BASE_DN = "ou=structures";

	/** Ldap Template de lecture */
	@Resource
	private LdapTemplate ldapTemplateRead;

	@Override
	public Name buildDn(StructureLdap o) throws LdapServiceException {
		if (o != null) {
			return buildDn(o.getSupannCodeEntite(), "structures");
		}
		throw new StructureException("Structure null");
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
	public void create(StructureLdap o) throws LdapServiceException {
		DirContextAdapter context = new DirContextAdapter(buildDn(o));
		mapToContext(o, context);
		ldapTemplateRead.bind(context);
	}

	@Override
	public void update(StructureLdap o) throws LdapServiceException {
		DirContextOperations context = ldapTemplateRead.lookupContext(buildDn(o));
		mapToContext(o, context);
		ldapTemplateRead.modifyAttributes(context);
	}

	@Override
	public void createOrUpdate(StructureLdap o) throws LdapServiceException {
		StructureLdap oLdap = findByPrimaryKey(o.getSupannCodeEntite());
		if (oLdap != null && StringUtils.hasText(oLdap.getSupannCodeEntite())) {
			update(o);
		} else {
			create(o);
		}
	}

	@Override
	public void delete(StructureLdap o) throws LdapServiceException {
		Name dn = buildDn(o);
		ldapTemplateRead.unbind(dn);
	}

	@Override
	public StructureLdap findByPrimaryKey(String code) throws LdapServiceException {
		StructureLdap r = new StructureLdap();
		r.setSupannCodeEntite(code);
		try {
			r = (StructureLdap) ldapTemplateRead.lookup(buildDn(r), getContextMapper());
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
	public StructureLdap findEntityByFilter(String filter) throws LdapServiceException {
		StructureLdap r = new StructureLdap();
		try {
			List<Object> l = ldapTemplateRead.search(BASE_DN, filter, getContextMapper());
			if (l != null && l.size() > 0) {
				r = (StructureLdap) l.get(0);
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
	public List<StructureLdap> findEntitiesByFilter(String filter) throws LdapServiceException {
		List<StructureLdap> l = null;
		try {
			String[] attributes = {"objectClass","udlLibelleAffichage","supannCodeEntite","modifyTimestamp"};
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
	public void mapToContext(StructureLdap o, DirContextOperations context) {
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

	public void mapAttributsToContext(StructureLdap o, DirContextOperations context) {
		addAttributToContext("objectclass", o.getObjectClass(), context);
		addAttributToContext("udlLibelleAffichage", o.getUdlLibelleAffichage(), context);
		addAttributToContext("supannCodeEntite", o.getSupannCodeEntite(), context);
		addAttributToContext("modifyTimestamp", o.getModifyTimestamp(), context);
		
	}

	@Override
	public ContextMapper getContextMapper() {
		return new StructureContextMapper();
	}

	private static class StructureContextMapper extends AbstractContextMapper {
		@Override
		public StructureLdap doMapFromContext(DirContextOperations context) {
			StructureLdap o = new StructureLdap();
			o.setObjectClass(context.getStringAttributes("objectClass"));
			o.setUdlLibelleAffichage(context.getStringAttribute("udlLibelleAffichage"));
			o.setSupannCodeEntite(context.getStringAttribute("supannCodeEntite"));
			o.setModifyTimestamp(context.getStringAttribute("modifyTimestamp"));
			
			return o;
		}
	}

}
