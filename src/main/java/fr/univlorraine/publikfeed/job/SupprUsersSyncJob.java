package fr.univlorraine.publikfeed.job;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.UserPublikController;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="supprUsersSyncJob")
@Slf4j
public class SupprUsersSyncJob {


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
		log.info("       START JOB "+JobUtils.SYNC_SUPPR_USERS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_SUPPR_USERS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {
			try {
				// Ajout timestamp du start dans la base
				ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_SUPPR_USERS_JOB);

				// Récupération des users
				List<UserHis> listLoginBdd = userHisService.findAllActiv();
				log.info(" {} users actifs dans Publik-feed", listLoginBdd !=null ?listLoginBdd.size() : 0);
				// Si on a des comptes à traiter
				if(listLoginBdd!=null && !listLoginBdd.isEmpty()) {
					// Filtre ldap
					String filtre = "(&(uid=*))";

					// Execution du filtre ldap
					try {
						log.info("execution du filtre ldap {} ...", filtre);
						List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(filtre);

						if(lp!=null && !lp.isEmpty()) {
							log.info("{} comptes ldap", lp.size());
							List<String> listLoginLdap = Utils.listPeopleToListLogin(lp);
							log.info("{} logins", listLoginLdap.size());
							lp=null;
							process.setNbObjTotal(listLoginBdd.size());
							process.setNbObjTraite(0);
							process.setNbObjErreur(0);

							// sauvegarde du nombre d'objets à traiter dans la base
							process = processHisService.update(process);

							// Pour chaque entrée Bdd
							for(UserHis u : listLoginBdd) {
								// Si le login n'est dans le ldap
								if(!listLoginLdap.contains(u.getLogin())) {
									// Suppression du user de Publik
									userPublikController.suppressionUser(u);
								}
								process.setNbObjTraite(process.getNbObjTraite() + 1);

								// sauvegarde du nombre d'objets traites dans la base
								process = processHisService.update(process);

							}
						}

					} catch (LdapServiceException e) {
						log.error("LdapServiceException lors de syncSupprUsers pour le filtre : "+filtre,e);
					}

				}
				// Ajout timestamp de fin dans la base
				processHisService.end(process);


				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_SUPPR_USERS_JOB);
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_SUPPR_USERS_JOB);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_SUPPR_USERS_JOB);
		log.info("###################################################");

	}
}
