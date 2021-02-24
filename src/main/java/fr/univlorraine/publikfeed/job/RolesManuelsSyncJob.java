package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.json.entity.ListUuidJson;
import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.json.entity.UuidJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.AddUserToRoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="rolesManuelsSyncJob")
@Slf4j
public class RolesManuelsSyncJob {

	@Resource
	private ProcessHisService processHisService;


	@Resource
	private RoleManuelService roleManuelService;

	@Resource
	private RolePublikApiService rolePublikApiService;
	
	@Resource
	private UserHisService userHisService;


	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;


	public void syncRoles() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_ROLES_MANUELS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_ROLES_MANUELS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {

			// Ajout timestamp du start dans la base
			ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_ROLES_MANUELS_JOB);

			// Recuperation des groupes actifs
			List<RoleManuel> lroles = roleManuelService.findActive();

			if(lroles != null ) {

				log.info("{} roles ", lroles.size());
				process.setNbObjTotal(lroles.size());
				process.setNbObjTraite(0);
				process.setNbObjErreur(0);

				for(RoleManuel role : lroles) {
					try {
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

						// Incrément du nombre d'objet traités
						process.setNbObjTraite(process.getNbObjTraite() + 1);

						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);
					} catch (Exception e) {
						log.warn("Erreur lors du traitement du role", role.getId());

						// Incrément du nombre d'objet traités
						process.setNbObjTraite(process.getNbObjTraite() + 1);

						// Incrément du nombre d'objet traités
						process.setNbObjErreur(process.getNbObjErreur() + 1);

						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);
					}
				}
			}

			// Ajout timestamp de fin dans la base
			processHisService.end(process);


			// Notifier l'arret du job
			JobUtils.stop(JobUtils.SYNC_ROLES_MANUELS_JOB);

		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_ROLES_MANUELS_JOB);
		log.info("###################################################");

	}
}
