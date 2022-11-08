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
package fr.univlorraine.publikfeed.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import lombok.extern.slf4j.Slf4j;

/**
 * Outils pour la gestion des jobs
 * @author Charlie Dubois
 */
@Slf4j
public final class JobUtils {

	public static final String CHECK_USERS_ANOMALIES_JOB = "CheckUserAnomalieJob";
	
	public static final String SYNC_SUPPR_USERS_JOB = "SupprSyncUsersJob";
	
	public static final String SYNC_NEW_USERS_JOB = "SyncNewUsersJob";
	
	public static final String SYNC_USERS_JOB = "SyncUsersJob";

	public static final String SYNC_RESP_ROLE_JOB = "SyncRespRoleJob";
	
	public static final String SYNC_ROLES_MANUELS_JOB = "SyncRolesManuelsJob";
	
	public static final String SUPPR_ROLES_MANUELS_INACTIFS = "SupprRolesManuelsInactifs";
	
	public static final String SUPPR_ROLES_UNITAIRES_JOB = "SupprRolesUnitJob";
	
	public static final String RUNNING = "RUNNING";
	
	public static final String OFF = "OFF";
	
	
	/** Map de liaison Semaphore / Job */
	public static final List<String> jobList = new LinkedList<>();

	

	
	
	static {
		jobList.add(JobUtils.SYNC_NEW_USERS_JOB);
		jobList.add(JobUtils.SYNC_USERS_JOB);
		jobList.add(JobUtils.SYNC_SUPPR_USERS_JOB);
		jobList.add(JobUtils.SYNC_RESP_ROLE_JOB);
		jobList.add(JobUtils.SYNC_ROLES_MANUELS_JOB);
		jobList.add(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS);
		jobList.add(JobUtils.CHECK_USERS_ANOMALIES_JOB);
		//jobList.add(JobUtils.SUPPR_ROLES_UNITAIRES_JOB);
	}


	public static String getStatus(String job) {
		Semaphore s = SemaphoreUtils.semaphoreJobMap.get(job);
		log.info("Semaphore : {}", s);
		if (s != null) {
			if (s.availablePermits() > 0) {
				return OFF;
			}
			return RUNNING;
		}
		return null;
	}

	public static boolean tryToStart(String job) {
		Semaphore s = SemaphoreUtils.semaphoreJobMap.get(job);
		log.info("Semaphore : {}", s);
		if (s != null) {
			try {
				if (SemaphoreUtils.isAvailablePermitsAndTryAcquire(s)) {
					return true;
				}
			} catch (InterruptedException e) {
				log.warn("InterruptedException lors de tryToStart "+job, e);
				return false;
			}
		}
		return false;
	}

	public static boolean stop(String job) {
		Semaphore s = SemaphoreUtils.semaphoreJobMap.get(job);
		log.info("Semaphore : {}", s);
		if (s != null) {
			SemaphoreUtils.release(s);
			return true;
		}
		return false;
	}


}
