package fr.univlorraine.publikfeed.cron;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.univlorraine.publikfeed.job.RolesManuelsSyncJob;
import fr.univlorraine.publikfeed.job.UsersSyncJob;
import fr.univlorraine.publikfeed.utils.CronUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@SuppressWarnings("serial")
@Slf4j
public class CronService  implements Serializable {

	
	@Autowired
	private UsersSyncJob usersSyncJob;
	
	@Autowired
	private RolesManuelsSyncJob rolesManuelsSyncJob;
	
	
	@Scheduled(cron = CronUtils.CRON_SYNC_USERS)
	public void cronJobSyncUsers() {
		usersSyncJob.syncUsers();
	}
	
	@Scheduled(cron = CronUtils.CRON_SYNC_ROLE_MANUEL)
	public void cronJobSyncRoleManuel() {
		rolesManuelsSyncJob.syncRoles();
	}
	
}
