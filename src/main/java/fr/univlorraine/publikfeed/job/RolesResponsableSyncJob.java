package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.RolePublikController;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.entity.StructureLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="rolesResponsableSyncJob")
@Slf4j
public class RolesResponsableSyncJob {



	@Value("${publik.default.resp.role.vide}")
	private transient String defaultUsers;

	@Value("${filtre.respsyncjob}")
	private transient String filtreRespSyncJob;

	@Value("${filtre.strrespsyncjob}")
	private transient String filtreStrRespSyncJob;

	@Resource
	private RolePublikController rolePublikController;

	@Resource
	private ProcessHisService processHisService;

	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;

	@Resource
	private LdapGenericService<StructureLdap> ldapStructureService;


	public void syncRoles() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_RESP_ROLE_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_RESP_ROLE_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {

			// Ajout timestamp du start dans la base
			ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_RESP_ROLE_JOB);

			// récupération du dernier ProcessHis pour le job avec date de fin non null
			ProcessHis lastExec = processHisService.getLastSuccessExc(JobUtils.SYNC_RESP_ROLE_JOB);

			// Date par défaut pour récupérer tous les comptes
			String dateLdap = "20120101010003Z";

			// calcul de la date evenement
			if(lastExec!=null && lastExec.getDatFin()!=null) {
				// On prend la date de début comme date max des maj ldap ignorées
				dateLdap = Utils.formatDateToLdap(lastExec.getId().getDatDeb());
			}

			// Filtre ldap qui recupère les personnes avec des roles de type A , 1 ou 2 ayant été mis à jour depuis le derniere run
			String filtre = filtreRespSyncJob;

			HashMap<String, List<String>> mapResponsables = new HashMap<String, List<String>> ();
			
			HashMap<String, String> mapLibelle = new HashMap<String, String> ();
 
			// Execution du filtre ldap
			try {
				log.info("execution du filtre ldap {} ...", filtre);
				List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(filtre);

				if(lp!=null && !lp.isEmpty()) {
					log.info("{} responsables dans le ldap", lp.size());
					process.setNbObjTotal(lp.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);

					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);
					try {
						// Pour chaque entrée ldap
						for(PeopleLdap p : lp) {

							// On parcourt les fonctions et on renseigne la map
							for(String udlFonction :  p.getUdlFonction()) {

								String codStr = udlFonction.split("\\[affect=")[1].split("\\]")[0].replaceFirst("\\{LOC\\}", "");
								String typeFct = udlFonction.split("\\[type=")[1].split("\\]")[0];

								// Si le type fait partie des types à gérer
								if(typeFct.equals("A") || typeFct.equals("1") || typeFct.equals("2")) {
									if(mapResponsables.containsKey(codStr)) {
										mapResponsables.get(codStr).add(p.getUid());
									} else {
										List<String> llogin = new LinkedList<String> ();
										llogin.add(p.getUid());
										mapResponsables.put(codStr, llogin);
									}
								}
							}

							// Incrément du nombre d'objet traités
							process.setNbObjTraite(process.getNbObjTraite() + 1);

							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);


						}



						// Récupération des responsables par défaut applicatif
						List<String> ldefaultlogins = null;
						if(StringUtils.hasText(defaultUsers)) {
							// ajout admins par defaut
							ldefaultlogins = Arrays.asList(defaultUsers.split(","));
						}

						// Récupération des structures
						String dateInstant = Utils.formatDateToLdap(LocalDateTime.now());
						String filtreStr = "(&"+filtreStrRespSyncJob+"(udlDateExpire>="+dateInstant+"))";
						log.info("execution du filtre ldap {} ...", filtreStr);
						List<StructureLdap> ls = ldapStructureService.findEntitiesByFilter(filtreStr);
						log.info("{} structures actives dans le ldap", ls.size());

						// Incrément du nombre d'objet à traiter
						process.setNbObjTotal(process.getNbObjTotal() + ls.size());

						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);

						// On parcourt les structures
						for(StructureLdap s : ls) {
							String codeStr = s.getSupannCodeEntite().replaceFirst("\\{LOC\\}", "");

							// On conserve le libellé
							mapLibelle.put(codeStr, s.getUdlLibelleAffichage());
							
							// Si la structure est non présente dans la map
							if(mapResponsables.containsKey(codeStr)) {
								log.debug("Structure {} : {} deja dans la map",codeStr, s.getUdlLibelleAffichage());
							} else {

								// Si aucun ajout avec les resp par défaut global
								if(!mapResponsables.containsKey(codeStr)) {
									log.info("Structure {} : {} non presente dans la map. Ajout avec le user par défaut",codeStr, s.getUdlLibelleAffichage());
									mapResponsables.put(codeStr, ldefaultlogins);
								}
							}


						}

						log.info("Map des responsables : {}", mapResponsables);


						// Maj d'une table spécifique (et maj dans publik si nécessaire)
						for (Entry<String, List<String>> structure : mapResponsables.entrySet()) {

							log.info("-{} / {}",structure.getKey(), structure.getValue());


							try {
								String libelle = mapLibelle.get(structure.getKey());
								// Maj du role dans la base et dans Publik
								rolePublikController.syncRoleResp(structure, libelle);


								// Incrément du nombre d'objet traités
								process.setNbObjTraite(process.getNbObjTraite() + 1);

								// sauvegarde du nombre d'objets traites dans la base
								process = processHisService.update(process);

							}catch(Exception e) {
								// Incrément du nombre d'objet traités
								process.setNbObjTraite(process.getNbObjTraite() + 1);
								// Incrément du compteur d'erreur
								process.setNbObjErreur(process.getNbObjErreur() + 1);
								// sauvegarde du nombre d'objets traites dans la base
								process = processHisService.update(process);

							}
						}

						}catch (Exception e) {
							log.warn("Exception lors du traitement du user",e);
							// Incrément du nombre d'objet traités
							process.setNbObjTraite(process.getNbObjTraite() + 1);
							// Incrément du compteur d'erreur
							process.setNbObjErreur(process.getNbObjErreur() + 1);
							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);
						}
					}

					// Ajout timestamp de fin dans la base
					processHisService.end(process);


				} catch (LdapServiceException e) {
					log.error("LdapServiceException lors de syncUsers pour le filtre : "+filtre,e);
				}




				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_RESP_ROLE_JOB);

			}
			log.info("###################################################");
			log.info("       END JOB "+JobUtils.SYNC_RESP_ROLE_JOB);
			log.info("###################################################");

		}
	}
