package fr.univlorraine.publikfeed.job;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.RolePublikController;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import lombok.extern.slf4j.Slf4j;

@Component(value="rolesManuelsSyncJob")
@Slf4j
public class RolesManuelsSyncJob {


	@Resource
	private RolePublikController rolePublikController;

	@Resource
	private ProcessHisService processHisService;


	@Resource
	private RoleManuelService roleManuelService;



	public void syncRoles() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_ROLES_MANUELS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_ROLES_MANUELS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {
			try {
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
							// Synchronisation du role
							rolePublikController.syncRoleManuel(role);

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
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SYNC_ROLES_MANUELS_JOB);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_ROLES_MANUELS_JOB);
		log.info("###################################################");

	}
}
