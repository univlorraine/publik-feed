package fr.univlorraine.publikfeed.job;


import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.UserPublikController;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.publik.services.UserPublikApiService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="checkUsersAnomalieJob")
@Slf4j
public class CheckUsersAnomalieJob {


	@Resource
	private UserPublikController userPublikController;
	
	@Resource
	private UserPublikApiService userPublikApiService;

	@Resource
	private ProcessHisService processHisService;


	@Resource
	private UserHisService userHisService;



	public void checkUsers() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.CHECK_USERS_ANOMALIES_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.CHECK_USERS_ANOMALIES_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {

			// Ajout timestamp du start dans la base
			ProcessHis process = processHisService.getNewProcess(JobUtils.CHECK_USERS_ANOMALIES_JOB);

			// Recuperation des users actifs
			List<UserHis> lusers = userHisService.findAllActiv();

			if(lusers != null ) {

				log.info("{} user ", lusers.size());
				process.setNbObjTotal(lusers.size());
				process.setNbObjTraite(0);
				process.setNbObjErreur(0);

				for(UserHis user : lusers) {
					try {
						// Si le user a un uuid publik renseigné
						if(StringUtils.hasText(user.getUuid())){
							// Récupération du user Publik
							UserPublikApi userPublik = userPublikApiService.getUserByUuid(user.getUuid());
							
							//Si le user récupéré est null ou ne colle pas avec le user en base
							if(userPublik== null || userPublik.getUsername()==null || !userPublik.getUsername().equals(user.getLogin()+Utils.EPPN_SUFFIX) ) {
								System.out.println("### CHECK_USERS_ANOMALIES_JOB - USER : " + user.getLogin() + "en anomalie");
								// Incrément du nombre d'objet traités
								process.setNbObjErreur(process.getNbObjErreur() + 1);
							}

						}
						// Incrément du nombre d'objet traités
						process.setNbObjTraite(process.getNbObjTraite() + 1);

						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);
					} catch (Exception e) {
						System.out.println("### CHECK_USERS_ANOMALIES_JOB - USER : " + user.getLogin() + "en anomalie");
						log.warn("### USER : {} en anomalie (exception)", user.getLogin());

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
			JobUtils.stop(JobUtils.CHECK_USERS_ANOMALIES_JOB);

		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.CHECK_USERS_ANOMALIES_JOB);
		log.info("###################################################");

	}
}
