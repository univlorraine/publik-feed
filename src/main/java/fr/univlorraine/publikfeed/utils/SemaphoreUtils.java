package fr.univlorraine.publikfeed.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** @author Matthieu MANGINOT / Charlie Dubois */
public class SemaphoreUtils {

	/** Sémaphore pour le job de sync de users */
	public static final Semaphore syncUserJob = new Semaphore(1);
	
	/** Sémaphore pour le job de sync des roles manuels */
	public static final Semaphore syncRoleManuel = new Semaphore(1);
	
	/** Sémaphore pour le job de suppression des role unitaires */
	public static final Semaphore supprUnitRoleJob = new Semaphore(1);


	/** Map de liaison Semaphore / Job */
	public static final Map<String, Semaphore> semaphoreJobMap = new HashMap<>();

	
	static {
		semaphoreJobMap.put(JobUtils.SYNC_USERS_JOB, syncUserJob);
		semaphoreJobMap.put(JobUtils.SYNC_ROLES_MANUELS_JOB, syncRoleManuel);
		semaphoreJobMap.put(JobUtils.SUPPR_ROLES_UNITAIRES_JOB, supprUnitRoleJob);
		
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
