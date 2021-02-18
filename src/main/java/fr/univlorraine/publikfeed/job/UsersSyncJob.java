package fr.univlorraine.publikfeed.job;


import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.exceptions.LdapServiceException;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="usersSyncJob")
@Slf4j
public class UsersSyncJob {

	@Resource
	private ProcessHisService processHisService;
	
	@Resource
	private UserHisService userHisService;
	
	
	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;


	public void syncUsers() {

		log.info("###################################################");
		log.info("       START JOB "+JobUtils.SYNC_USERS_JOB);
		log.info("###################################################");


		// Vérifier que le job n'est pas déjà en cours
		if (!JobUtils.tryToStart(JobUtils.SYNC_USERS_JOB)) {
			log.error("JOB ALREADY RUNNING");
		} else {

			// Ajout timestamp du start dans la base
			ProcessHis process = processHisService.getNewProcess(JobUtils.SYNC_USERS_JOB);

			// récupération du dernier ProcessHis pour le job avec date de fin non null
			ProcessHis lastExec = processHisService.getLastSuccessExc(JobUtils.SYNC_USERS_JOB);
			
			// Date par défaut pour récupérer tous les comptes
			String dateLdap = "20120101010003Z";
			
			// calcul de la date evenement
			if(lastExec!=null && lastExec.getDatFin()!=null) {
				dateLdap = Utils.formatDateToLdap(lastExec.getDatFin());
			}

			// Filtre ldap
			String filtre = "(&(uid=duboi*)(eduPersonPrincipalName=*)(modifytimestamp>=" + dateLdap + "))";
			
			// Execution du filtre ldap
			try {
				log.info("execution du filtre ldap {} ...", filtre);
				List<PeopleLdap> lp = ldapPeopleService.findEntitiesByFilter(filtre);
				
				if(lp!=null && !lp.isEmpty()) {
					log.info("{} comptes mis à jour depuis la derniere execution", lp.size());
					process.setNbObjTotal(lp.size());
					process.setNbObjTraite(0);
					process.setNbObjErreur(0);
					
					// sauvegarde du nombre d'objets à traiter dans la base
					process = processHisService.update(process);
					
					// Pour chaque entrée ldap
					for(PeopleLdap p : lp) {
						// Conversion du compte en donnée JSON à envoyer à Publik
						UserJson userLdap = Utils.getUserJson(p);
						
						// Récupération de la derniere synchro du compte sauvegardée dans la base
						Optional<UserHis> ouh = userHisService.find(p.getUid());
						
						// Si on a aucune entrée en base
						if(!ouh.isPresent()) {
							// TODO On doit créer le user dans Publik
							
						} else {
							boolean userToUpdate = true;
							// On regarde si on doit maj le user dans Publik en comparant le json avec l'entrée ldap
							UserJson userBdd = Utils.getUserJson(ouh.get());
							
							// On maj si les 2 users sont différents
							userToUpdate = !userLdap.equals(userBdd);
							
							if(userToUpdate) {
								// TODO maj le user dans Publik
							}
						}
						
						process.setNbObjTraite(process.getNbObjTraite() + 1);
						// sauvegarde du nombre d'objets traites dans la base
						process = processHisService.update(process);
					}
				}
				
				// Ajout timestamp de fin dans la base
				processHisService.end(process);
			
				
			} catch (LdapServiceException e) {
				log.error("LdapServiceException lors de syncUsers pour le filtre : "+filtre,e);
			}
			
			
			
			
			// Notifier l'arret du job
			JobUtils.stop(JobUtils.SYNC_USERS_JOB);

		}
		log.info("###################################################");
		log.info("       END JOB "+JobUtils.SYNC_USERS_JOB);
		log.info("###################################################");

	}
}
