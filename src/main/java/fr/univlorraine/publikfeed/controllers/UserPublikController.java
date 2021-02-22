package fr.univlorraine.publikfeed.controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.publik.services.UserPublikApiService;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="userPublikController")
@Slf4j
public class UserPublikController {


	@Resource
	private UserHisService userHisService;


	@Resource
	private UserPublikApiService userPublikApiService;

	public void createOrUpdateUser(PeopleLdap p) throws Exception {

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

		}else {
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
		
		// TODO Vérification des roles
		createOrUpdateRole(p);

	}
	
	public void createOrUpdateRole(PeopleLdap p) throws Exception {
		
	}

}
