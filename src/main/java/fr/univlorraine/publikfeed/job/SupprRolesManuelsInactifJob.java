package fr.univlorraine.publikfeed.job;


import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.controllers.RolePublikController;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import lombok.extern.slf4j.Slf4j;

@Component(value="supprRolesManuelsInactifJob")
@Slf4j
public class SupprRolesManuelsInactifJob {

	@Value("${publik.default.user.role.vide}")
	private transient String defaultUsers;


	@Resource
	private RolePublikController rolePublikController;

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
		log.info("       START JOB "+JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS)) {
			log.error("JOB ALREADY RUNNING");
		} else {
			try {
				// Ajout timestamp du start dans la base
				ProcessHis process = processHisService.getNewProcess(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);

				// Recuperation des groupes actifs
				List<RoleManuel> lroles = roleManuelService.findInactiveAndInPublik();

				if(lroles != null ) {

					log.info("{} roles ", lroles.size());
					process.setNbObjTotal(lroles.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);

					for(RoleManuel role : lroles) {
						try {
							boolean isDeleted = rolePublikController.deleteRoleInPublik(role);

							if(!isDeleted) {
								// Incrément du nombre d'objet traités en erreur
								process.setNbObjErreur(process.getNbObjErreur() + 1);
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
				JobUtils.stop(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);
		log.info("###################################################");

	}
}
