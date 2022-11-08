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
	 * supprime le role dans publik et maj la bdd avec la date de suppression
	 * @param role
	 * @return
	 */
	public boolean deleteRoleInPublik(RoleResp role) {
		// On supprime le role dans Publik
		boolean isDeleted = rolePublikApiService.deleteRole(role.getUuid());

		// Si la suppression s'est bien passée
		if(isDeleted) {
			// On met à jour la base
			role.setDatSupPublik(LocalDateTime.now());
			roleRespService.saveRole(role);
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
					// Si la personne n'est pas déjà dans Publik
					if(uuid == null)  {
						// Création de la personne
						if(userPublikController.createOrUpdateUser(p.getUid())) {
							uuid = userHisService.getUuidFromLogin(p.getUid());
						}
					}
					// Si on a la personne dans Publik
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

		// Si le role n'est pas dans Publik et que le rôle possede des users
		if(role.getDatCrePublik()==null && !uuids.isEmpty()) {
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

		// Si le rôle est dans Publik et qu'il n'a aucun user
		if(role.getUuid()!=null && role.getDatCrePublik()!=null && role.getDatSupPublik()==null && uuids.isEmpty()) {
			log.info("Role {} sans user => On le supprime automatiquement de Publik", role.getId());
			//suppression du rôle dans Publik
			deleteRoleInPublik(role);
		}else {
			// Si nouveau role ou si le hash est different
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
		if(structure.getValue()!=null) {
			// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
			Collections.sort(structure.getValue());
			r.setLogins(String.join(",", structure.getValue()));
		}else{
			r.setLogins(null);
		}
		r.setDatMaj(LocalDateTime.now());
		// Maj des logins dans la base
		r = roleRespService.saveRole(r);

		// calcul de la liste des uuids des resp
		List<String> uuids = new LinkedList<String> ();
		if(structure.getValue()!=null) {
			for(String login : structure.getValue()) {
				log.info("Recuperation uuid de {}", login);
				String uuid = userHisService.getUuidFromLogin(login);
				if(uuid != null) {
					// Ajout du login à la liste
					uuids.add(uuid);
				}
			}
			// On trie de la liste pour avoir un hash identique si la liste contient les même éléments
			Collections.sort(uuids);
		}

		log.info("{} users dans le groupe {}", uuids.size(), r.getCodStr());

		//Creation de l'objet Json correspondant à la liste
		ListUuidJson data = Utils.getListUuidJson(uuids);

		// Creation du hash
		String hash = Utils.getHash(data);

		log.info("Hash du role resp {} : {}", r.getCodStr(), hash);
		boolean newRole = false;

		// Si le role n'est pas dans Publik et qu'il a des personnes associées
		if(r.getDatCrePublik()==null && !uuids.isEmpty() ) {
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

		// Si le role a des user et qu'il est nouveau ou si le hash est different
		if( !uuids.isEmpty() && (newRole || (r.getHash()==null && hash!=null) || (r.getHash()!=null && hash==null) || !r.getHash().equals(hash))) {

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

		// Si le rôle est vide et qu'il est toujours présent dans Publik
		if(r.getDatCrePublik()!=null && uuids.isEmpty() && r.getDatSupPublik()==null) {
			log.info("Suppression roleResp {} dans publik ", r.getCodStr());
			//suppression du role
			deleteRoleInPublik(r);
		}

	}

}
