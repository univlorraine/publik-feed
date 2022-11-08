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
			try {
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
			} catch (Exception e) {
				// Notifier l'arret du job
				JobUtils.stop(JobUtils.CHECK_USERS_ANOMALIES_JOB);
				throw e;
			}
		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.CHECK_USERS_ANOMALIES_JOB);
		log.info("###################################################");

	}
}
