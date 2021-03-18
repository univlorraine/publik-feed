package fr.univlorraine.publikfeed.publik.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
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


	/**
	 * Recherche un user dans Publik
	 * @param username
	 * @return
	 */
	public UserPublikApi getUserByUsername(String username) {
		log.info("getUserByUsername :  {}", username );
		// On récupère l'URL de l'api
		String purl = apiUrl + USERS;
		purl += "?username={username}";

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", username);


		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " username=>" + username);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));
		//Appelle du WS qui retourne le user publik
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.GET, Utils.createRequest(null), String.class, params);

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
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(user), String.class, params);

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
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.PUT, Utils.createRequest(user), String.class, params);

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
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.DELETE,null, String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response.getStatusCode().equals(HttpStatus.OK) || response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
			log.info("Suppression user reponse OK : {}", response.getStatusCode());
			return true;
		}
		return false;
	}


}
