package fr.univlorraine.publikfeed.utils;

/**
 * Outils pour la gestion des cron
 * @author Charlie Dubois
 */
public final class CronUtils {

	public static final String CRON_SYNC_USERS = "0 50 7 * * ?";
	
	public static final String CRON_SYNC_NEW_USERS = "0 0 * * * ?";
	
	public static final String CRON_SYNC_SUPPR_USERS = "0 50 8 * * ?";
	
	public static final String CRON_SYNC_ROLE_MANUEL = "0 30 6 * * ?";
	
	public static final String CRON_SYNC_ROLE_RESP = "0 40 6 * * ?";
	
	public static final String CRON_SUPPR_ROLE_MANUEL_INACTIF = "0 5 7 * * ?";


	
}
