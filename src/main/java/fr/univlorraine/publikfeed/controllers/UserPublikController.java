package fr.univlorraine.publikfeed.controllers;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.ldap.services.LdapGenericService;
import fr.univlorraine.publikfeed.model.app.entity.RoleAuto;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.entity.UserRolePK;
import fr.univlorraine.publikfeed.model.app.services.RoleAutoService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.AddUserToRoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import fr.univlorraine.publikfeed.publik.services.UserPublikApiService;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="userPublikController")
@Slf4j
public class UserPublikController {


	@Resource
	private UserHisService userHisService;

	@Resource
	private RoleAutoService roleAutoService;

	@Resource
	private UserPublikApiService userPublikApiService;

	@Resource
	private RolePublikApiService rolePublikApiService;


	@Resource
	private LdapGenericService<PeopleLdap> ldapPeopleService;


	public boolean createOrUpdateUser(String login) {
		try {
			PeopleLdap p = ldapPeopleService.findByPrimaryKey(login);
			if(p!=null) {
				return createOrUpdateUser(p);
			}
		} catch (Exception e) {
			log.warn("Une exception est survenue pendant la création de "+login + " dans Publik",e);
		}
		return false;
	} 

	public boolean createOrUpdateUser(PeopleLdap p) throws Exception {

		String userUuid=null;

		// Mapper JSON
		ObjectMapper objectMapper = new ObjectMapper();

		// Conversion du compte en donnée JSON à envoyer à Publik
		UserJson userLdap = Utils.getUserJson(p);

		log.debug("modifytimestamp de {} : {}",p.getUid(), p.getModifyTimestamp());

		// Récupération de la derniere synchro du compte sauvegardée dans la base
		Optional<UserHis> ouh = userHisService.find(p.getUid());

		// Si le user n'est pas dans la base ou si on ne l'a pas déjà traité depuis sa dernière maj ldap
		if(!ouh.isPresent() || ouh.get().getDatMaj()==null || p.getModifyTimestamp()==null || ouh.get().getDatMaj().isBefore(Utils.getDateFromLdap(p.getModifyTimestamp()))) {
			// Si on a aucune entrée en base
			if(!ouh.isPresent()) {
				log.info("{} non present en base", p.getUid());
				// On regarde si il existe déjà dans publik
				UserPublikApi userPublik = userPublikApiService.getUserByUsername(p.getEduPersonPrincipalName());

				if(userPublik !=null) {
					log.info("{} present dans Publik", p.getUid());
					// On créé le user dans la base
					UserHis newUser = new UserHis();
					newUser.setUuid(userPublik.getUuid());
					newUser.setLogin(p.getUid());
					//newUser = userHisService.save(newUser);
					// On pousse le user dans l'optional
					ouh = Optional.of(newUser);
				} else {
					log.info("{} non present dans Publik", p.getUid());
				}
			} else {
				log.info("{} present en base", p.getUid());
			}

			// Si on a toujours aucune entrée en base ou qu'il est supprimé dans publik
			if(!ouh.isPresent() || ouh.get().getDatSup()!=null) {
				log.info("Le compte {} doit etre créé dans Publik",p.getUid());
				// créer le user dans Publik
				UserPublikApi response = userPublikApiService.createUser(userLdap);

				// Si la réponse contient des données
				if(response != null) {
					log.info("Le compte {} a été créé dans Publik",p.getUid());
					userUuid = response.getUuid();
					UserHis newUser = new UserHis();
					newUser.setUuid(response.getUuid());
					newUser.setLogin(p.getUid());
					try {
						newUser.setData(objectMapper.writeValueAsString(userLdap));
					} catch (JsonProcessingException e) {
						log.warn("Probleme a la serialiazation JSON de :"+userLdap,e);
					}
					newUser.setDatMaj(LocalDateTime.now());
					//Maj bdd
					newUser = userHisService.save(newUser);
					log.info("Le compte {} a été mis a jour dans la base",p.getUid());
				}

			} else {

				userUuid = ouh.get().getUuid();
				boolean userToUpdate = true;

				// Si le user en base a des data associees
				if(ouh.get().getData() != null) {
					// On regarde si on doit maj le user dans Publik en comparant le json avec l'entrée ldap
					UserJson userBdd = Utils.getUserJson(ouh.get());

					// On maj si les 2 users sont différents
					userToUpdate = !userLdap.equals(userBdd);
				}

				if(userToUpdate) {

					log.info("Le compte {} doit etre mis a jour dans Publik",p.getUid());
					// maj le user dans Publik
					UserPublikApi response = userPublikApiService.updateUser(userLdap, ouh.get().getUuid());

					// Si la réponse contient des données
					if(response != null) {
						log.info("Le compte {} a été mis à jour dans Publik",p.getUid());
						UserHis user = ouh.get();
						try {
							user.setData(objectMapper.writeValueAsString(userLdap));

						} catch (JsonProcessingException e) {
							log.warn("Probleme a la serialiazation JSON de :"+userLdap,e);
						}
						user.setDatMaj(LocalDateTime.now());
						//Maj bdd
						user = userHisService.save(user);
						log.info("Le compte {} a été mis a jour dans la base",p.getUid());
					}
				} else {
					// inutile de pousser les infos du compte dans Publik car les attributs nécessaire a publik ne sont pas impactes
					UserHis user = ouh.get();
					// on maj la date dans la base pour ne pas refaire le comparatif des data au prochain run
					user.setDatMaj(LocalDateTime.now());
					//Maj bdd
					user = userHisService.save(user);
					log.info("Le compte {} est déjà à jour dans Publik",p.getUid());
				}
			}

			// Vérification des roles nominatifs
			checkRolesUnitaires(p, userUuid);

		} else {
			log.info("{} dateMaj superieure au modifytimestamp. Compte non traite", p.getUid());
		}
		return true;

	}

	public void checkRolesUnitaires(PeopleLdap p,  String userUuid) throws Exception {
		List<String> listeRole = new LinkedList<String> ();

		// Si ce n'est pas un compte étudiant 
		if(Utils.isNotStudent(p)) {	
			// Traitement role unitaire nominatif
			String roleName =  getRoleUnitairePersonnel(p.getEduPersonPrincipalName());
			createOrUpdateRole(roleName, p.getUid(), userUuid);
			listeRole.add(roleName);
		}

		// Role Pers UL
		if(StringUtils.hasText(p.getSupannEmpId())) {
			String roleEmpName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_PERSONNEL;
			createOrUpdateRole(roleEmpName, p.getUid(), userUuid);
			listeRole.add(roleEmpName);
		}

		// Role Etu UL
		if(StringUtils.hasText(p.getSupannEtuId())) {
			String roleEtuName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_ETUDIANT;
			createOrUpdateRole(roleEtuName, p.getUid(), userUuid);
			listeRole.add(roleEtuName);
		}

		// Role par BC
		if(p.getUdlCategories()!=null && p.getUdlCategories().length > 0) {
			for(String bc : p.getUdlCategories()) {
				String roleBcName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_BC + Utils.ROLE_SEPARATOR + bc;
				createOrUpdateRole(roleBcName, p.getUid(), userUuid);
				listeRole.add(roleBcName);
			}
		}

		// Récupération de tous les roles de la personne dans la base
		List<UserRole> listeUserRole = roleAutoService.findRolesFromLogin(p.getUid());
		if(listeUserRole !=null && !listeUserRole.isEmpty()) {
			log.info("{} possede {} roles unitaires dans la base", p.getUid(), listeUserRole.size());
			for(UserRole ur : listeUserRole) {
				// Si le role est actif et qu'il n'est pas dans la liste
				if(ur!=null && ur.getDatSup() == null && !listeRole.contains(ur.getId().getRoleId())) {

					log.info("{} ne possede plus le role {}. Il doit etre supprime de Publik", p.getUid(), ur.getId().getRoleId());

					// Recuperation du role pour avoir son uuid
					Optional<RoleAuto> or = roleAutoService.findRole(ur.getId().getRoleId());

					// Si on a bien récupéré le role et qu'on a un uuid publik associé
					if(or.isPresent() && or.get().getUuid() != null) {

						// Suppression de la personne des roles Publik
						if(rolePublikApiService.deleteUserFromRole(userUuid, or.get().getUuid())) {

							log.info(" {} supprimee du role {} ({}) dans Publik", p.getUid(), ur.getId().getRoleId(), or.get().getUuid());

							// Maj bdd
							ur.setDatMaj(LocalDateTime.now());
							ur.setDatSup(LocalDateTime.now());
							ur = roleAutoService.saveUserRole(ur);

							log.info("userRole {} - {} mis a jour dans la base", ur.getId().getLogin(), ur.getId().getRoleId());
						}
					}
				}
			}
		}

	}

	public void createOrUpdateRole(String roleName, String login , String userUuid) throws Exception {

		boolean just_created = false;
		// Recherche du role dans la base
		Optional<RoleAuto> optRole = roleAutoService.findRole(roleName);
		RoleAuto role = null;

		// Si le role n'existe pas ou plus
		if(!optRole.isPresent() || optRole.get().getDatSup() != null) {
			// Creation du role dans Publik
			RoleJson rj = new RoleJson();
			rj.setName(roleName);
			RolePublikApi rolePublik = rolePublikApiService.createRole(rj);
			just_created = true;

			// maj BDD
			role = new RoleAuto();
			role.setId(roleName);
			role.setUuid(rolePublik.getUuid());
			role.setSlug(rolePublik.getSlug());
			role.setOu(rolePublik.getOu());
			role.setDatMaj(LocalDateTime.now());
			role = roleAutoService.saveRole(role);

		} else {
			role= optRole.get();
		}

		// Si le role vient d'etre créé ou si la personne n'a pas (ou plus) le role
		if(just_created || !userPossedeRole(login, role.getId()) ) {

			// Ajout du role à la personne dans Publik
			AddUserToRoleResponsePublikApi utr = rolePublikApiService.addUserToRole(userUuid, role.getUuid());

			// MAJ BDD
			UserRole ur = new UserRole();
			UserRolePK urpk = new UserRolePK();
			urpk.setLogin(login);
			urpk.setRoleId(role.getId());
			ur.setId(urpk);
			ur.setDatMaj(LocalDateTime.now());
			ur = roleAutoService.saveUserRole(ur);
		}
	}

	private boolean userPossedeRole(String login, String roleId) {
		Optional<UserRole> ru = roleAutoService.findUserRole(login, roleId);
		// Si le role est associe au user sans date de suppression
		if(ru.isPresent() && ru.get().getDatSup() == null) {
			return true;
		}
		return false;
	}

	public void ajoutUuidsFromLogin(List<String> uuids, String logins) {
		// ajout admins par defaut
		String[] tlogins = logins.split(",");
		if(tlogins!=null && tlogins.length>0) {
			for(String login : tlogins) {
				//recuperation du uuid
				String uuid = userHisService.getUuidFromLogin(login);
				// Si on a un uuid et qu'il est pas déjà dans la liste
				if(uuid != null && !uuids.contains(uuid)) {
					// Ajout du login à la liste
					uuids.add(uuid);
				} else {
					log.info("Uuid de {} non trouve ou deja dans la liste, uuid : ", login, uuid);
				}
			}
		}

	}

	public boolean suppressionUser(UserHis user) {

		// Si on a l'uuid publik 
		if(StringUtils.hasText(user.getUuid()) ) {
			// Suppression du user dans publik
			if(userPublikApiService.deleteUser(user.getUuid())) {

				// récupération de l'uuid du role spécifique à la personne
				String roleEppnId = getRoleUnitairePersonnel(user.getLogin() + Utils.EPPN_SUFFIX);

				// Si le user possede le role eppn dans la base
				Optional<UserRole> roleEppn = roleAutoService.findUserRole(user.getLogin(), roleEppnId);
				if(roleEppn.isPresent()) {
					// Récupération du role dans la base
					Optional<RoleAuto> roleAuto = roleAutoService.findRole(roleEppnId);
					// Si on a trouvé le role dans la base
					if(roleAuto.isPresent()) {
						// Suppression du role spécifique de la personne dans publik
						if(rolePublikApiService.deleteRole(roleAuto.get().getUuid())) {
							log.info(" Role {} ({}) supprimé dans Publik", roleAuto.get().getId(), roleAuto.get().getUuid());

							// Maj bdd du lien avec le user
							roleEppn.get().setDatMaj(LocalDateTime.now());
							roleEppn.get().setDatSup(LocalDateTime.now());
							UserRole userRoleEppnBdd = roleAutoService.saveUserRole(roleEppn.get());

							log.info("Date suppr du lien User - Role : {} - {} mis a jour dans la base",userRoleEppnBdd.getId().getLogin(), userRoleEppnBdd.getId().getRoleId());

							// Maj bdd du role
							roleAuto.get().setDatMaj(LocalDateTime.now());
							roleAuto.get().setDatSup(LocalDateTime.now());
							RoleAuto roleEppnBdd = roleAutoService.saveRole(roleAuto.get());

							log.info("Date suppr du Role {} mis a jour dans la base", roleEppnBdd.getDatSup());

						}

					}
				}

				// suppression dans la base de tous les roles associés à la personne (fait automatiquement dans publik lors de la suppr de la personne)
				List<UserRole> listRole = roleAutoService.findRolesFromLogin(user.getLogin());
				if(listRole != null && !listRole.isEmpty()) {
					for(UserRole ur : listRole) {
						if(ur!=null && ur.getDatSup()==null) {
							ur.setDatSup(LocalDateTime.now());
							ur = roleAutoService.saveUserRole(ur);
							log.info("Date suppr du lien User - Role : {} - {} mis a jour dans la base", ur.getId().getLogin(), ur.getId().getRoleId());

						}
					}
				}

				// TODO Probleme des roles qui se retrouvent potentiellement vide?

				// maj des roles de la personne dans la base de données
				user.setDatSup(LocalDateTime.now());
				user = userHisService.save(user);
				return true;
			}
		}

		return false;
	}

	private String getRoleUnitairePersonnel(String eppn) {
		return Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_NOMINATIF + eppn;
	}

	public List<UserPublikApi> getLastModified(LocalDateTime dateLastRun) {
		String lastModifiedDate = Utils.formatDateForPublik(dateLastRun);
		return userPublikApiService.getUserLastModified(lastModifiedDate);
	}




}
