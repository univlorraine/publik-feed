package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.ErrorController;
import fr.univlorraine.publikfeed.controllers.UserPublikController;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.UserErrHisService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="usersSyncJob")
@Slf4j
public class UsersSyncJob {


	@Value("${filtre.userssyncjob}")
	private transient String filtreSyncUser;

	@Resource
	private UserPublikController userPublikController;

	@Resource
	private ErrorController errorController;

	@Resource
	private ProcessHisService processHisService;


	@Resource
	private UserErrHisService userErrHisService;

	@Resource
	private UserHisService userHisService;

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
			try {
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

				// Récupération des users en anomalie (user_err_his dont date supérieure à la date de derniere maj dans user_his) 
				List<String> listLoginToRetry = userErrHisService.getUserToRetry();
				if(listLoginToRetry!=null && !listLoginToRetry.isEmpty()) {
					process.setNbObjTotal(listLoginToRetry.size());
					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);
					log.info("{} comptes en anomalie a repasser", listLoginToRetry.size());
				} else {
					log.info("aucun compte en anomalie");
				}

				// Filtre ldap
				String filtre = "(&"+filtreSyncUser+"(modifytimestamp>=" + dateLdap + "))";

				// Execution du filtre ldap
				try {
					log.info("execution du filtre ldap {} ...", filtre);
					List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(filtre);

					if(lp!=null && !lp.isEmpty()) {
						log.info("{} comptes mis à jour depuis la derniere execution", lp.size());
						process.setNbObjTotal(process.getNbObjTotal() + lp.size());
						process.setNbObjTraite(0);
						process.setNbObjErreur(0);

						// sauvegarde du nombre d'objets à traiter dans la base
						process = processHisService.update(process);

						// Pour chaque entrée ldap
						for(PeopleLdap p : lp) {
							// Si la personne a un mail
							if(p != null && StringUtils.hasText(p.getMail())) {
								// Si ce n'est pas un étudiant ou si il est déjà connu dans la base
								if(Utils.isNotStudent(p) || userHisService.find(p.getUid()).isPresent()) {
									try {
										// Création ou maj de l'utilisateur dans Publik
										userPublikController.createOrUpdateUser(p);

										// Incrément du nombre d'objet traités
										process.setNbObjTraite(process.getNbObjTraite() + 1);

										// sauvegarde du nombre d'objets traites dans la base
										process = processHisService.update(process);

									}catch (Exception e) {
										log.warn("Exception lors du traitement du user",e);
										// Incrément du nombre d'objet traités
										process.setNbObjTraite(process.getNbObjTraite() + 1);
										// Incrément du compteur d'erreur
										process.setNbObjErreur(process.getNbObjErreur() + 1);
										// sauvegarde du nombre d'objets traites dans la base
										process = processHisService.update(process);
										// gestion de l'erreur
										errorController.check(e, p);
									}
								} else {
									// Ne pas traiter le compte 
									// Incrément du nombre d'objet traités
									process.setNbObjTraite(process.getNbObjTraite() + 1);

									// sauvegarde du nombre d'objets traites dans la base
									process = processHisService.update(process);
								}
							}

						}
					}

				} catch (LdapServiceException e) {
					log.error("LdapServiceException lors de syncUsers pour le filtre : "+filtre,e);
				}




				if(listLoginToRetry!=null && !listLoginToRetry.isEmpty()) {

					for(String login : listLoginToRetry) {
						log.info("Retry user from login : {}", login);
						try {

							PeopleLdap p = ldapPeopleService.findByPrimaryKey(login);

							if(p!=null) {
								// Création ou maj de l'utilisateur dans Publik
								userPublikController.createOrUpdateUser(p);

								// Incrément du nombre d'objet traités
								process.setNbObjTraite(process.getNbObjTraite() + 1);

								// sauvegarde du nombre d'objets traites dans la base
								process = processHisService.update(process);
							}

						}catch (Exception e) {
							log.warn("Exception lors du traitement du user",e);
							// Incrément du nombre d'objet traités
							process.setNbObjTraite(process.getNbObjTraite() + 1);
							// Incrément du compteur d'erreur
							process.setNbObjErreur(process.getNbObjErreur() + 1);
							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);

							//sauvegarde de l'erreur dans la base
							UserErrHis erreur = new UserErrHis();
							erreur.setLogin(login);
							erreur.setTrace(e.getMessage());
							erreur = userErrHisService.save(erreur);
						}

					}
				}


				// Ajout timestamp de fin dans la base
				processHisService.end(process);


				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_USERS_JOB);
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_USERS_JOB);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_USERS_JOB);
		log.info("###################################################");

	}
}
