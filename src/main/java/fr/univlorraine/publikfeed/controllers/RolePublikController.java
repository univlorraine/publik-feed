package fr.univlorraine.publikfeed.controllers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.json.entity.ListUuidJson;
import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.entity.RoleResp;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.model.app.services.RoleRespService;
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
	private RoleRespService roleRespService;

	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;

	@Resource
	private UserHisService userHisService;

	@Resource
	private UserPublikController userPublikController;

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

		// Si la liste est vide et qu'on a des logins par defaut pour le role
		if(uuids.isEmpty() && StringUtils.hasText(role.getLoginsDefaut())) {
			log.info("Ajout logins par defaut dans le groupe {} vide : {}", role.getId(), role.getLoginsDefaut());
			userPublikController.ajoutUuidsFromLogin(uuids, role.getLoginsDefaut());
		}

		// Si la liste est vide et qu'on a des users par defaut définis globalement dans l'application
		if(uuids.isEmpty() && defaultUsers != null) {
			log.info("Ajout users par defaut dans le groupe {} vide : {}", role.getId(), defaultUsers);
			userPublikController.ajoutUuidsFromLogin(uuids, defaultUsers);
		}

		// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
		Collections.sort(uuids);

		log.info("{} users dans le groupe {}", uuids.size(), role.getId());


		//Creation de l'objet Json correspondant à la liste
		ListUuidJson data = Utils.getListUuidJson(uuids);

		// Creation du hash
		String hash = Utils.getHash(data);

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



	public void syncRoleResp(Entry<String, List<String>> structure, String libelle) {

		Optional<RoleResp> role = roleRespService.findRole(structure.getKey());
		RoleResp r = null;
		// Si le role n'était pas déjà dans la base
		if(!role.isPresent()) {
			r = new RoleResp();
			r.setCodStr(structure.getKey());
			r.setLibelle(libelle);
		} else {
			r = role.get();
			// maj du libellé au cas ou
			r.setLibelle(libelle);
		}
		// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
		Collections.sort(structure.getValue());
		r.setLogins(String.join(",", structure.getValue()));
		r.setDatMaj(LocalDateTime.now());
		// Maj des logins dans la base
		r = roleRespService.saveRole(r);

		// calcul de la liste des uuids des resp
		List<String> uuids = new LinkedList<String> ();
		for(String login : structure.getValue()) {
			//recuperation du uuid
			String uuid = userHisService.getUuidFromLogin(login);
			if(uuid != null) {
				// Ajout du login à la liste
				uuids.add(uuid);
			}
		}

		// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
		Collections.sort(uuids);

		log.info("{} users dans le groupe {}", uuids.size(), r.getCodStr());

		//Creation de l'objet Json correspondant à la liste
		ListUuidJson data = Utils.getListUuidJson(uuids);

		// Creation du hash
		String hash = Utils.getHash(data);

		log.info("Hash du role resp {} : {}", r.getCodStr(), hash);
		boolean newRole = false;

		// Si le role n'est pas dans Publik
		if(r.getDatCrePublik()==null) {
			log.info("Role {} est nouveau. Il doit etre cree dans Publik", r.getCodStr());
			RoleJson rj = new RoleJson();
			rj.setName(Utils.PREFIX_ROLE_RESP + r.getCodStr().toUpperCase());
			// Creation dans Publik si necessaire
			RolePublikApi rolePublik = rolePublikApiService.createRole(rj);

			log.info("Role {} créé dans Publik", r.getCodStr());

			newRole = true;
			// maj BDD
			r.setUuid(rolePublik.getUuid());
			r.setSlug(rolePublik.getSlug());
			r.setOu(rolePublik.getOu());
			r.setDatCrePublik(LocalDateTime.now());
			r = roleRespService.saveRole(r);

			log.info("Uuid du Role {} sauvegardé dans la base : ", r.getCodStr(), r.getUuid());

		}

		// Si nouvau role ou si le hash est different
		if(newRole || (r.getHash()==null && hash!=null) || (r.getHash()!=null && hash==null) || !r.getHash().equals(hash)) {

			log.info("La population du role {} doit être mise à jour dans Publik", r.getCodStr());
			// Ajout/maj des personnes dans publik
			AddUserToRoleResponsePublikApi response = rolePublikApiService.setUsersToRole(r.getUuid(), data);

			log.info("Users Role {} ont été mis à jour dans Publik : ", r.getCodStr());

			// Si l'appel à l'API Publik s'est bien passé
			if(response!=null && response.getResult()==1) {
				// Maj de la date et du hash dans la base
				r.setDatMajPublik(LocalDateTime.now());
				r.setHash(hash);
				r = roleRespService.saveRole(r);
				log.info("Hash du Role {} sauvegardé dans la base : ", r.getCodStr(), r.getHash());
			}
		} else {
			log.info("La population du role {} est déjà à jour dans Publik", r.getCodStr());
		}

	}

}
