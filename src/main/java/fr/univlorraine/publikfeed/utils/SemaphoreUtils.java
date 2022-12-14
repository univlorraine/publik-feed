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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** @author Matthieu MANGINOT / Charlie Dubois */
public class SemaphoreUtils {

	/** Sémaphore pour le job de sync de users */
	public static final Semaphore syncUserJob = new Semaphore(1);
	
	/** Sémaphore pour le job de sync de users */
	public static final Semaphore checkUserAnomalieJob = new Semaphore(1);
	
	/** Sémaphore pour le job de sync de users */
	public static final Semaphore syncNewUserJob = new Semaphore(1);
	
	/** Sémaphore pour le job de sync de users */
	public static final Semaphore syncSupprUserJob = new Semaphore(1);
	
	/** Sémaphore pour le job de sync des roles manuels */
	public static final Semaphore syncRoleManuel = new Semaphore(1);
	
	/** Sémaphore pour le job de sync des roles de responsable */
	public static final Semaphore syncRespRole = new Semaphore(1);
	
	/** Sémaphore pour le job de sync des roles manuels */
	public static final Semaphore supprRoleManuelInactif = new Semaphore(1);
	
	/** Sémaphore pour le job de suppression des role unitaires */
	public static final Semaphore supprUnitRoleJob = new Semaphore(1);


	/** Map de liaison Semaphore / Job */
	public static final Map<String, Semaphore> semaphoreJobMap = new HashMap<>();

	
	static {
		semaphoreJobMap.put(JobUtils.SYNC_USERS_JOB, syncUserJob);
		semaphoreJobMap.put(JobUtils.SYNC_SUPPR_USERS_JOB, syncSupprUserJob);
		semaphoreJobMap.put(JobUtils.SYNC_NEW_USERS_JOB, syncNewUserJob);
		semaphoreJobMap.put(JobUtils.SYNC_ROLES_MANUELS_JOB, syncRoleManuel);
		semaphoreJobMap.put(JobUtils.SUPPR_ROLES_UNITAIRES_JOB, supprUnitRoleJob);
		semaphoreJobMap.put(JobUtils.SUPPR_ROLES_MANUELS_INACTIFS, supprRoleManuelInactif);
		semaphoreJobMap.put(JobUtils.SYNC_RESP_ROLE_JOB, syncRespRole);
		semaphoreJobMap.put(JobUtils.CHECK_USERS_ANOMALIES_JOB, checkUserAnomalieJob);
		
	}

	/** {@link Semaphore#tryAcquire()} */
	public static boolean tryAcquire(final Semaphore s) throws InterruptedException {
		return s.tryAcquire(180, TimeUnit.SECONDS);
	}

	/** Retourne vrai si un jeton est disponible pour le sémaphore en paramètre */
	public static boolean isAvailablePermits(final Semaphore s) {
		boolean isAvailablePermits = false;
		if (s.availablePermits() > 0) {
			isAvailablePermits = true;
		}
		return isAvailablePermits;
	}

	/** Retourne vrai si
	 * un jeton est disponible pour le sémaphore en paramètre
	 * et que celui est acquis */
	public static boolean isAvailablePermitsAndTryAcquire(final Semaphore s) throws InterruptedException {
		boolean is = false;
		if (isAvailablePermits(s) && tryAcquire(s)) {
			is = true;
		}
		return is;
	}

	/** {@link Semaphore#release()} */
	public static void release(final Semaphore s) {
		s.release();
	}
}
