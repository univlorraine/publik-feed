package fr.univlorraine.publikfeed.job.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univlorraine.publikfeed.job.UsersSyncJob;
import fr.univlorraine.publikfeed.utils.JobUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobLauncher {


	@Autowired
	private UsersSyncJob usersSyncJob;


	public void launch(String jobName) {
		switch(jobName) {
			case JobUtils.SYNC_USERS_JOB : 
				usersSyncJob.syncUsers();
				break;
		}
	}
}
