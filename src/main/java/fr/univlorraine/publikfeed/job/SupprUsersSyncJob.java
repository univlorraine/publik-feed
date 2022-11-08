/**
 *
 * Copyright (c) 2022 Université de Lorraine, 18/02/2021
 *
 * dn-sied-dev@univ-lorraine.fr
 *
 * Ce logiciel est un programme informatique servant à alimenter Publik depuis des groupes LDAP.
 *
 * Ce logiciel est régi par la licence CeCILL 2.1 soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL 2.1, et que vous en avez accepté les
 * termes.
 *
 */
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
