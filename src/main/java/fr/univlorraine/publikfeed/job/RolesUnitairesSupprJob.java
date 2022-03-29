package fr.univlorraine.publikfeed.job;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.RolePublikController;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="roleUnitSupprJob")
@Slf4j
public class RolesUnitairesSupprJob {

	@Resource
	private RolePublikController rolePublikController;

	@Resource
	private RolePublikApiService rolePublikApiService;

	@Resource
	private ProcessHisService processHisService;


	public void deleteAllRoles() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SUPPR_ROLES_UNITAIRES_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SUPPR_ROLES_UNITAIRES_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {
			try {
				// Ajout timestamp du start dans la base
				ProcessHis process = processHisService.getNewProcess(JobUtils.SUPPR_ROLES_UNITAIRES_JOB);

				// Recuperation des roles
				List<RolePublikApi> lroles= rolePublikController.getAllRoles(Utils.PREFIX_ROLE_UNITAIRE);

				if(lroles!=null && !lroles.isEmpty()) {
					log.info("{} roles présents dans Publik", lroles.size());
					process.setNbObjTotal(lroles.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);

					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);

					// Pour chaque role
					for(RolePublikApi r : lroles) {

						try {
							if(r!=null && r.getUuid()!=null) {
								//suppression du role
								if(!rolePublikApiService.deleteRole(r.getUuid())) {
									// Incrément du compteur d'erreur
									process.setNbObjErreur(process.getNbObjErreur() + 1);
								}
								// Incrément du nombre d'objet traités
								process.setNbObjTraite(process.getNbObjTraite() + 1);
								// sauvegarde du nombre d'objets traites dans la base
								process = processHisService.update(process);
							}
						}catch (Exception e) {
							// Incrément du nombre d'objet traités
							process.setNbObjTraite(process.getNbObjTraite() + 1);
							// Incrément du compteur d'erreur
							process.setNbObjErreur(process.getNbObjErreur() + 1);
							// sauvegarde du nombre d'objets traites dans la base
							process = processHisService.update(process);

						}

					}
				}

				// Ajout timestamp de fin dans la base
				processHisService.end(process);


				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SUPPR_ROLES_UNITAIRES_JOB);
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SUPPR_ROLES_UNITAIRES_JOB);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SUPPR_ROLES_UNITAIRES_JOB);
		log.info("###################################################");

	}
}
