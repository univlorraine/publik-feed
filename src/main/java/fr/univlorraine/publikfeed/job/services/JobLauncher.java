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
package fr.univlorraine.publikfeed.job.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univlorraine.publikfeed.job.CheckUsersAnomalieJob;
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
	private CheckUsersAnomalieJob checkUsersAnomalieJob;
	
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
			case JobUtils.CHECK_USERS_ANOMALIES_JOB :
				checkUsersAnomalieJob.checkUsers();
				break;
		}
	}
}
