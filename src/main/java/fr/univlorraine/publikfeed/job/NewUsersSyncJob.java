package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.UserPublikController;
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
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="newUsersSyncJob")
@Slf4j
public class NewUsersSyncJob {


	@Value("${filtre.userssyncjob}")
	private transient String filtreSyncUser;

	@Resource
	private UserPublikController userPublikController;

	@Resource
	private ProcessHisService processHisService;


	@Resource
	private UserHisService userHisService;



	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;


	public void syncUsers() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_NEW_USERS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_NEW_USERS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {
			try {
				// Ajout timestamp du start dans la base
				ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_NEW_USERS_JOB);

				// récupération du dernier ProcessHis pour le job avec date de fin non null
				ProcessHis lastExec = processHisService.getLastSuccessExc(JobUtils.SYNC_NEW_USERS_JOB);

				// Par défaut j - 6 mois
				LocalDateTime dateLastRun = LocalDateTime.now().minusMonths(6);

				// calcul de la date evenement
				if(lastExec!=null && lastExec.getDatFin()!=null) {
					// On prend la date de début comme date max des maj ldap ignorées
					dateLastRun = lastExec.getId().getDatDeb();
				}

				//Synchro des users créés dans Publik et qu'on a pas encore dans la base (les étudiants notamment)
				List<UserPublikApi> listNewUsers = userPublikController.getLastModified(dateLastRun);
				if(listNewUsers!=null && !listNewUsers.isEmpty()) {

					process.setNbObjTotal(listNewUsers.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);

					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);

					// Pour chaque user modifié dans Publik
					for(UserPublikApi userPublik : listNewUsers) {
						// Si c'est un user UL
						if(userPublik != null && userPublik.getUsername() != null && userPublik.getUsername().contains(Utils.EPPN_SUFFIX)) {
							// Récupération du login
							String login = userPublik.getUsername().split("@")[0];
							// Si pas dans la base
							if(!userHisService.find(login).isPresent()) {
								log.info("{} présent dans Publik mais pas en base", login);
								userPublikController.createOrUpdateUser(login);
							}
						}
						process.setNbObjTraite(process.getNbObjTraite() + 1);

						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);
					}
				}

				// Ajout timestamp de fin dans la base
				processHisService.end(process);


				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_NEW_USERS_JOB);
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_NEW_USERS_JOB);
				throw e;
			}

		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_NEW_USERS_JOB);
		log.info("###################################################");

	}
}
