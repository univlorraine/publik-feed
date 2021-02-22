package fr.univlorraine.publikfeed.controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.model.app.entity.Role;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.entity.UserRolePK;
import fr.univlorraine.publikfeed.model.app.services.RoleService;
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
	private RoleService roleService;

	@Resource
	private UserPublikApiService userPublikApiService;

	@Resource
	private RolePublikApiService rolePublikApiService;
	
	public void createOrUpdateUser(PeopleLdap p) throws Exception {

		String userUuid=null;
		
		// Mapper JSON
		ObjectMapper objectMapper = new ObjectMapper();

		// Conversion du compte en donnée JSON à envoyer à Publik
		UserJson userLdap = Utils.getUserJson(p);

		// Récupération de la derniere synchro du compte sauvegardée dans la base
		Optional<UserHis> ouh = userHisService.find(p.getUid());

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

		// Si on a toujours aucune entrée en base
		if(!ouh.isPresent()) {
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
				log.info("Le compte {} est déjà à jour dans Publik",p.getUid());
			}
		}

		// Vérification des roles nominatifs
		checkRolesUnitaires(p, userUuid);

	}

	public void checkRolesUnitaires(PeopleLdap p,  String userUuid) throws Exception {

		// Role unitaire nominatif
		String roleName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_NOMINATIF + p.getEduPersonPrincipalName();
		createOrUpdateRole(roleName, p.getUid(), userUuid);

		// Role Pers UL
		if(StringUtils.hasText(p.getSupannEmpId())) {
			String roleEmpName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_PERSONNEL + p.getEduPersonPrincipalName();
			createOrUpdateRole(roleEmpName, p.getUid(), userUuid);
		}

		// Role Etu UL
		if(StringUtils.hasText(p.getSupannEtuId())) {
			String roleEtuName = Utils.PREFIX_ROLE_UNITAIRE + Utils.PREFIX_ROLE_ETUDIANT + p.getEduPersonPrincipalName();
			createOrUpdateRole(roleEtuName, p.getUid(), userUuid);
		}

		// Role par BC
		if(p.getUdlCategories()!=null && p.getUdlCategories().length > 0) {
			for(String bc : p.getUdlCategories()) {
				String roleBcName = Utils.PREFIX_ROLE_UNITAIRE + bc + Utils.ROLE_SEPARATOR + p.getEduPersonPrincipalName();
				createOrUpdateRole(roleBcName, p.getUid(), userUuid);
			}
		}


	}

	public void createOrUpdateRole(String roleName, String login , String userUuid) throws Exception {

		boolean just_created = false;
		// Recherche du role dans la base
		Optional<Role> optRole = roleService.findRole(roleName);
		Role role = null;
		
		// Si le role n'existe pas ou plus
		if(!optRole.isPresent() || optRole.get().getDatSup() != null) {
			// Creation du role dans Publik
			RoleJson rj = new RoleJson();
			rj.setName(roleName);
			RolePublikApi rolePublik = rolePublikApiService.createRole(rj);
			just_created = true;
			
			// maj BDD
			role = new Role();
			role.setId(roleName);
			role.setUuid(rolePublik.getUuid());
			role.setSlug(rolePublik.getSlug());
			role.setOu(rolePublik.getOu());
			role.setDatMaj(LocalDateTime.now());
			role = roleService.saveRole(role);
				
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
				ur = roleService.saveUserRole(ur);
		}
	}

	private boolean userPossedeRole(String login, String roleId) {
		Optional<UserRole> ru = roleService.findUserRole(login, roleId);
		// Si le role est associe au user sans date de suppression
		if(ru.isPresent() && ru.get().getDatSup() == null) {
			return true;
		}
		return false;
	}

}
