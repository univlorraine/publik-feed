package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.UserErrHisService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.publik.services.UserPublikApiService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="usersSyncJob")
@Slf4j
public class UsersSyncJob {

	@Resource
	private ProcessHisService processHisService;

	@Resource
	private UserHisService userHisService;
	
	@Resource
	private UserErrHisService userErrHisService;

	@Resource
	private UserPublikApiService userPublikApiService;


	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;


	public void syncUsers() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_USERS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_USERS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {

			// Ajout timestamp du start dans la base
			ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_USERS_JOB);

			// récupération du dernier ProcessHis pour le job avec date de fin non null
			ProcessHis lastExec = processHisService.getLastSuccessExc(JobUtils.SYNC_USERS_JOB);

			// Date par défaut pour récupérer tous les comptes
			String dateLdap = "20120101010003Z";

			// calcul de la date evenement
			if(lastExec!=null && lastExec.getDatFin()!=null) {
				// On prend la date de début comme date max des maj ldap ignorées
				dateLdap = Utils.formatDateToLdap(lastExec.getId().getDatDeb());
			}

			// Filtre ldap
			String filtre = "(&(uid=duboi*)(eduPersonPrincipalName=*)(modifytimestamp>=" + dateLdap + "))";

			// Execution du filtre ldap
			try {
				log.info("execution du filtre ldap {} ...", filtre);
				List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(filtre);

				if(lp!=null && !lp.isEmpty()) {
					log.info("{} comptes mis à jour depuis la derniere execution", lp.size());
					process.setNbObjTotal(lp.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);

					// Mapper JSON
					ObjectMapper objectMapper = new ObjectMapper();

					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);

					// Pour chaque entrée ldap
					for(PeopleLdap p : lp) {

						try {

							// Conversion du compte en donnée JSON à envoyer à Publik
							UserJson userLdap = Utils.getUserJson(p);

							// Récupération de la derniere synchro du compte sauvegardée dans la base
							Optional<UserHis> ouh = userHisService.find(p.getUid());

							// Si on a aucune entrée en base
							if(!ouh.isPresent()) {
								log.info("{} non present en base", p.getUid());
								// On regarde si il existe déjà dans publik
								UserPublikApi userPublik = userPublikApiService.getUserByUsername(p.getEduPersonPrincipalName());

								if(userPublik !=null) {
									log.info("{} present dans Publik", p.getUid());
									// On créé le user dans la base
									UserHis newUser = new UserHis();
									newUser.setUuid(userPublik.getUuid());
									newUser.setLogin(p.getUid());
									//newUser = userHisService.save(newUser);
									// On pousse le user dans l'optional
									ouh = Optional.of(newUser);
								} else {
									log.info("{} non present dans Publik", p.getUid());
								}
							} else {
								log.info("{} present en base", p.getUid());
							}

							// Si on a toujours aucune entrée en base
							if(!ouh.isPresent()) {
								log.info("Le compte {} doit etre créé dans Publik",p.getUid());
								// créer le user dans Publik
								UserPublikApi response = userPublikApiService.createUser(userLdap);

								// Si la réponse contient des données
								if(response != null) {
									log.info("Le compte {} a été créé dans Publik",p.getUid());
									UserHis newUser = new UserHis();
									newUser.setUuid(response.getUuid());
									newUser.setLogin(p.getUid());
									try {
										newUser.setData(objectMapper.writeValueAsString(userLdap));
									} catch (JsonProcessingException e) {
										log.warn("Probleme a la serialiazation JSON de :"+userLdap,e);
									}
									newUser.setDatMaj(LocalDateTime.now());
									//Maj bdd
									newUser = userHisService.save(newUser);
									log.info("Le compte {} a été mis a jour dans la base",p.getUid());
								}

							}else {
								boolean userToUpdate = true;

								// Si le user en base a des data associees
								if(ouh.get().getData() != null) {
									// On regarde si on doit maj le user dans Publik en comparant le json avec l'entrée ldap
									UserJson userBdd = Utils.getUserJson(ouh.get());

									// On maj si les 2 users sont différents
									userToUpdate = !userLdap.equals(userBdd);
								}

								if(userToUpdate) {

									log.info("Le compte {} doit etre mis a jour dans Publik",p.getUid());
									// maj le user dans Publik
									UserPublikApi response = userPublikApiService.updateUser(userLdap, ouh.get().getUuid());

									// Si la réponse contient des données
									if(response != null) {
										log.info("Le compte {} a été mis à jour dans Publik",p.getUid());
										UserHis user = ouh.get();
										try {
											user.setData(objectMapper.writeValueAsString(userLdap));

										} catch (JsonProcessingException e) {
											log.warn("Probleme a la serialiazation JSON de :"+userLdap,e);
										}
										user.setDatMaj(LocalDateTime.now());
										//Maj bdd
										user = userHisService.save(user);
										log.info("Le compte {} a été mis a jour dans la base",p.getUid());
									}
								} else {
									log.info("Le compte {} est déjà à jour dans Publik",p.getUid());
								}
							}
							// Incrément du nombre d'objet traités
							process.setNbObjTraite(process.getNbObjTraite() + 1);

							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);

						}catch (Exception e) {
							// Incrément du compteur d'erreur
							process.setNbObjErreur(process.getNbObjErreur() + 1);
							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);
							
							//sauvegarde de l'erreur dans la base
							UserErrHis erreur = new UserErrHis();
							erreur.setLogin(p.getUid());
							erreur.setTrace(e.getMessage());
							erreur = userErrHisService.save(erreur);
						}

					}
				}

				// Ajout timestamp de fin dans la base
				processHisService.end(process);


			} catch (LdapServiceException e) {
				log.error("LdapServiceException lors de syncUsers pour le filtre : "+filtre,e);
			}




			// Notifier l'arret du job
			JobUtils.stop(JobUtils.SYNC_USERS_JOB);

		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_USERS_JOB);
		log.info("###################################################");

	}
}
