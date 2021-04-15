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
