package fr.univlorraine.publikfeed.controllers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.json.entity.ListUuidJson;
import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.json.entity.UuidJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.AddUserToRoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="rolePublikController")
@Slf4j
public class RolePublikController {


	@Value("${publik.default.user.role.vide}")
	private transient String defaultUsers;
	
	@Resource
	private RolePublikApiService rolePublikApiService;
	
	@Resource
	private RoleManuelService roleManuelService;
	
	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;
	
	@Resource
	private UserHisService userHisService;
	
	/**
	 * Retourne tous les roles présents dans Publik
	 * @return
	 */
	public List<RolePublikApi> getAllRoles(String prefix){
		List<RolePublikApi> listRoles = new LinkedList<RolePublikApi> ();
		RoleResponsePublikApi result = rolePublikApiService.getRoles(null);
		listRoles.addAll(result.getResults());
		while(StringUtils.hasText(result.getNext())) {
			result = rolePublikApiService.getRoles(result.getNext());
			listRoles.addAll(result.getResults());
		}
		// Si un prefix est renseigné
		if(StringUtils.hasText(prefix)) {
			//suppression des roles ne remplissant pas la condition du prefix
			Predicate<RolePublikApi> rolePredicate = r -> !r.getName().startsWith(prefix);
			listRoles.removeIf(rolePredicate);
		}
		return listRoles;
	}

	/**
	 * supprime le role dans publik et maj la bdd avec la date de suppression
	 * @param role
	 * @return
	 */
	public boolean deleteRoleInPublik(RoleManuel role) {
		// On supprime le role dans Publik
		boolean isDeleted = rolePublikApiService.deleteRole(role.getUuid());

		// Si la suppression s'est bien passée
		if(isDeleted) {
			// On met à jour la base
			role.setDatSupPublik(LocalDateTime.now());
			roleManuelService.saveRole(role);
		} 
		return isDeleted;
	}
	
	
	/**
	 * Effectue la synchro d'un role manuel
	 * @param role
	 * @throws Exception
	 */
	public void syncRoleManuel(RoleManuel role) throws Exception {
		
		boolean newRole = false;
		List<String> uuids = new LinkedList<String> ();
		if(StringUtils.hasText(role.getFiltre())) {
			// Filtre ldap
			log.info("execution du filtre ldap {} ...", role.getFiltre());

			List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(role.getFiltre());

			if(lp!=null && !lp.isEmpty()) {
				for(PeopleLdap p : lp) {
					//recuperation du uuid
					String uuid = userHisService.getUuidFromLogin(p.getUid());
					if(uuid != null) {
						// Ajout du login à la liste
						uuids.add(uuid);
					}
				}
			}
		}
		// Si le role a des logins forcés
		if(StringUtils.hasText(role.getLogins())) {
			String[] tlogins = role.getLogins().split(",");
			if(tlogins!=null && tlogins.length>0) {
				for(String login : tlogins) {
					//recuperation du uuid
					String uuid = userHisService.getUuidFromLogin(login);
					// Si on a un uuid et qu'il est pas déjà dans la liste
					if(uuid != null && !uuids.contains(uuid)) {
						// Ajout du login à la liste
						uuids.add(uuid);
					} else {
						log.info("Uuid de {} non trouve ou deja dans la liste, uuid : ", login, uuid);
					}
				}
			}
		}
		
		// Si la liste est vide et qu'on a des user par defaut
		if(uuids.isEmpty() && defaultUsers != null) {
				log.info("Ajout users par defaut dans le groupe {} vide", role.getId());
				// ajout admins par defaut
				String[] tlogins = defaultUsers.split(",");
				if(tlogins!=null && tlogins.length>0) {
					for(String login : tlogins) {
						//recuperation du uuid
						String uuid = userHisService.getUuidFromLogin(login);
						// Si on a un uuid et qu'il est pas déjà dans la liste
						if(uuid != null && !uuids.contains(uuid)) {
							// Ajout du login à la liste
							uuids.add(uuid);
						} else {
							log.info("Uuid de {} non trouve ou deja dans la liste, uuid : ", login, uuid);
						}
					}
				}
		}
		// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
		Collections.sort(uuids);
		
		log.info("{} users dans le groupe {}", uuids.size(), role.getId());
		
		//Creation de l'objet Json correspondant à la liste
		ListUuidJson data = new ListUuidJson();
		List<UuidJson> liste = new LinkedList<UuidJson> ();
		// Ajout des uuids dans la liste
		for(String uuid : uuids) {
			UuidJson j = new UuidJson();
			j.setUuid(uuid);
			liste.add(j);
		}
		// Ajout de la liste à l'objet json
		data.setData(liste);
		
		String hash = null;
		// Si il y a des uuids dans le role
		if(!liste.isEmpty()) {
			//  Creation du hash à partir de l'objet
			hash = String.valueOf(data.hashCode());
		}
		
		log.info("Hash du role {} : {}", role.getId(), hash);
		
		// Si le role n'est pas dans Publik
		if(role.getDatCrePublik()==null) {
			log.info("Role {} est nouveau. Il doit etre cree dans Publik", role.getId());
			RoleJson rj = new RoleJson();
			rj.setName(Utils.PREFIX_ROLE_MANUEL + role.getId().toUpperCase());
			// Creation dans Publik si necessaire
			RolePublikApi rolePublik = rolePublikApiService.createRole(rj);
			
			log.info("Role {} créé dans Publik", role.getId());
			
			newRole = true;
			// maj BDD
			role.setId(role.getId().toUpperCase());
			role.setUuid(rolePublik.getUuid());
			role.setSlug(rolePublik.getSlug());
			role.setOu(rolePublik.getOu());
			role.setDatCrePublik(LocalDateTime.now());
			role = roleManuelService.saveRole(role);
			
			log.info("Uuid du Role {} sauvegardé dans la base : ", role.getId(), role.getUuid());
			
		}


		// Si nouvau role ou si le hash est different
		if(newRole || (role.getHash()==null && hash!=null) || (role.getHash()!=null && hash==null) || !role.getHash().equals(hash)) {
				
				log.info("La population du role {} doit être mise à jour dans Publik", role.getId());
				// Ajout/maj des personnes dans publik
				AddUserToRoleResponsePublikApi response = rolePublikApiService.setUsersToRole(role.getUuid(), data);

				log.info("Users Role {} ont été mis à jour dans Publik : ", role.getId());
				
				// Si l'appel à l'API Publik s'est bien passé
				if(response!=null && response.getResult()==1) {
					// Maj de la date et du hash dans la base
					role.setDatMajPublik(LocalDateTime.now());
					role.setHash(hash);
					role = roleManuelService.saveRole(role);
					log.info("Hash du Role {} sauvegardé dans la base : ", role.getId(), role.getHash());
				}
		} else {
			log.info("La population du role {} est déjà à jour dans Publik", role.getId());
		}
	}
 
}
