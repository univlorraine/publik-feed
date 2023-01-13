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
package fr.univlorraine.publikfeed.publik.services;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.publik.entity.UserResponsePublikApi;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class UserPublikApiService {


	private static final String USERS = "users";

	@Value("${publik.api.username}")
	private transient String apiUsername;

	@Value("${publik.api.password}")
	private transient String apiPassword;

	@Value("${publik.api.baseurl}")
	private transient String apiUrl;

	@Value("${publik.api.gravitee.header}")
	private transient String graviteeHeader;

	@Value("${publik.api.gravitee.apikey}")
	private transient String graviteeKey;



	public UserPublikApi getUserByUuid(String uuid) {
		log.info("getUserByUuid :  {}", uuid );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/{uuid}/";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("uuid", uuid);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " uuid=>" + uuid);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		try {
			//Appelle du WS qui retourne le user publik
			ResponseEntity<String> response = rt.exchange(purl, HttpMethod.GET, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

			log.info("Publik Response :" + response);

			//Si on a eu une réponse de type OK ou NOT_FOUND
			if (response != null && response.getStatusCode()!=null && (response.getStatusCode().equals(HttpStatus.NOT_FOUND) || response.getStatusCode().equals(HttpStatus.OK))) {
				// Si not found
				if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					return null;
				}
				try {
					// Si user recupéré
					if (response.getStatusCode().equals(HttpStatus.OK)) {
						ObjectMapper objectMapper = new ObjectMapper();
						UserPublikApi upar = objectMapper.readValue(response.getBody(), UserPublikApi.class);
						return upar;
					}

				} catch (Exception e) {
					log.error("Erreur ObjectMapper lors de getUserByUuid pour "+uuid, e);
				}
			}

		}catch(HttpClientErrorException hcee) {
			if(hcee != null && hcee.getStatusCode().is4xxClientError() && hcee.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				if(hcee.getResponseBodyAsString()!=null && hcee.getResponseBodyAsString().contains("Pas trouvé.")) {
					log.warn("User non trouvé. Uuid {} not found", uuid);
					return null;
				}
			} 
			throw hcee;
		}

		return null;

	}

	/**
	 * Recherche un user dans Publik
	 * @param username
	 * @return
	 */
	public UserPublikApi getUserByUsername(String username) {
		log.info("getUserByUsername :  {}", username );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/";
		purl += "?username={username}";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", username);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " username=>" + username);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		//Appelle du WS qui retourne le user publik
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.GET, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			UserResponsePublikApi upar;
			try {
				upar = objectMapper.readValue(response.getBody(), UserResponsePublikApi.class);

				//Si la réponse contient des users
				if(upar != null && upar.getResults()!=null && upar.getResults().size() == 1) {
					return upar.getResults().get(0);
				}
			} catch (Exception e) {
				log.warn("Erreur lors du traitement du resultat de getUserByUsername pour "+username, e);
			} 
		}
		return null;

	}



	/**
	 * Créer un user dans Publik
	 * @param user
	 * @return
	 */
	public UserPublikApi createUser(UserJson user) {

		log.info("createUser :  {}", user.getUsername() );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/";

		//Body
		Map<String,String> params = new HashMap<String,String>();

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " data : " + user );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le user Publik
		@SuppressWarnings("unchecked")
		//ResponseEntity<String> response =  (ResponseEntity<String>) rt.postForObject(purl, createRequestFromObject(user),ResponseEntity.class);
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(user, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			UserPublikApi upa;
			try {
				upa = objectMapper.readValue(response.getBody(), UserPublikApi.class);
				return upa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement du resultat de createUser pour "+user.getUsername(), e);
			} 
		}
		return null;

	}

	/**
	 * Met à jour un utilisateur Publik
	 * @param user
	 * @param uuid
	 * @return
	 */
	public UserPublikApi updateUser(UserJson user, String uuid) {

		log.info("updateUser :  {}", user.getUsername() );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/{uuid}/";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("uuid", uuid);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " uuid =>" + uuid);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		//Appelle du WS qui retourne le user publik
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.PUT, Utils.createRequest(user, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			UserPublikApi upa;
			try {
				upa = objectMapper.readValue(response.getBody(), UserPublikApi.class);
				return upa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement du resultat de updateUser pour "+user.getUsername(), e);
			} 
		}
		return null;

	}



	public boolean deleteUser(String uuid) {
		log.info("deleteUser : {}", uuid);
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/{uuid}/";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("uuid", uuid);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " uuid =>" + uuid);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		//Appelle du WS qui retourne le user publik
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.DELETE,Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response.getStatusCode().equals(HttpStatus.OK) || response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
			log.info("Suppression user reponse OK : {}", response.getStatusCode());
			return true;
		}
		return false;
	}



	public List<UserPublikApi> getUserLastModified(String modifiedDate) {
		List<UserPublikApi> listUsers = new LinkedList<UserPublikApi> ();
		log.info("getUserLastModified :  {}", modifiedDate );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS + "/";
		purl += "?modified__gte={modifiedDate}";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("modifiedDate", modifiedDate);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " modifiedDate=>" + modifiedDate);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		//Appelle du WS qui retourne le user publik
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.GET, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		while (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			UserResponsePublikApi upar;
			try {
				upar = objectMapper.readValue(response.getBody(), UserResponsePublikApi.class);
				// On réinit la réponse
				response = null;
				//Si la réponse contient des users
				if(upar != null && upar.getResults()!=null && !upar.getResults().isEmpty()) {
					listUsers.addAll(upar.getResults());
				}
				// Si il reste des résultats non retournés
				if(upar!=null && StringUtils.hasText(upar.getNext())) {
					// On récupère la suite
					log.info("Récupération du résultat suivant (next) : {}", upar.getNext());
					try {
						response = rt.getForEntity(new URI(upar.getNext()), String.class);
					} catch (Exception e) {
						log.error("Erreur lors du traitement du resultat next de getUserLastModified pour " + upar.getNext(), e);
					} 
				}
			} catch (Exception e) {
				log.warn("Erreur lors du traitement du resultat de getUserLastModified pour " + modifiedDate, e);
			} 
		}
		return listUsers;

	}






}
