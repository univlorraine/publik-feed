package fr.univlorraine.publikfeed.job.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univlorraine.publikfeed.job.NewUsersSyncJob;
import fr.univlorraine.publikfeed.job.RolesManuelsSyncJob;
import fr.univlorraine.publikfeed.job.RolesResponsableSyncJob;
import fr.univlorraine.publikfeed.job.RolesUnitairesSupprJob;
import fr.univlorraine.publikfeed.job.SupprRolesManuelsInactifJob;
import fr.univlorraine.publikfeed.job.SupprUsersSyncJob;
import fr.univlorraine.publikfeed.job.UsersSyncJob;
import fr.univlorraine.publikfeed.utils.JobUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobLauncher {


	@Autowired
	private UsersSyncJob usersSyncJob;
	
	@Autowired
	private NewUsersSyncJob newUsersSyncJob;
	
	@Autowired
	private SupprUsersSyncJob supprUsersSyncJob;
	
	@Autowired
	private RolesUnitairesSupprJob supprRoleUnitaireJob;
	
	@Autowired
	private SupprRolesManuelsInactifJob supprRolesManuelsInactifJob;
	
	@Autowired
	private RolesManuelsSyncJob rolesManuelsSyncJob;
	
	@Autowired
	private RolesResponsableSyncJob rolesResponsableSyncJob;


	public void launch(String jobName) {
		switch(jobName) {
			case JobUtils.SYNC_NEW_USERS_JOB : 
				newUsersSyncJob.syncUsers();
				break;
			case JobUtils.SYNC_USERS_JOB : 
				usersSyncJob.syncUsers();
				break;
			case JobUtils.SYNC_SUPPR_USERS_JOB : 
				supprUsersSyncJob.syncUsers();
				break;
			case JobUtils.SYNC_ROLES_MANUELS_JOB :
				rolesManuelsSyncJob.syncRoles();
				break;
			case JobUtils.SUPPR_ROLES_MANUELS_INACTIFS :
				supprRolesManuelsInactifJob.syncRoles();
				break;
			case JobUtils.SUPPR_ROLES_UNITAIRES_JOB : 
				supprRoleUnitaireJob.deleteAllRoles();
				break;
			case JobUtils.SYNC_RESP_ROLE_JOB :
				rolesResponsableSyncJob.syncRoles();
				break;
		}
	}
}
