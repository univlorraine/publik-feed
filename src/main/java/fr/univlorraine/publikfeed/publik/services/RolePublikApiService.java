package fr.univlorraine.publikfeed.publik.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.publik.entity.AddUserToRoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.entity.UserPublikApi;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class RolePublikApiService {


	private static final String ROLES = "roles";

	@Value("${publik.api.username}")
	private transient String apiUsername;

	@Value("${publik.api.password}")
	private transient String apiPassword;

	@Value("${publik.api.baseurl}")
	private transient String apiUrl;




	/**
	 * Créer un role dans Publik
	 * @param role
	 * @return
	 */
	public RolePublikApi createRole(RoleJson role) {

		log.info("createRole :  {} shortId : {} ou : {}", role.getName(), role.getSlug(), role.getOu() );
		// On récupère l'URL de l'api
		String purl = apiUrl + ROLES + "/?get_or_create=name";

		//Body
		Map<String,String> params = new HashMap<String,String>();

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " data : " + role );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le role Publik
		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(role), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			RolePublikApi rpa;
			try {
				rpa = objectMapper.readValue(response.getBody(), RolePublikApi.class);
				return rpa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement du resultat de createUser pour "+role.getName(), e);
			} 
		}
		return null;

	}
	
	
	
	
	/**
	 * Ajouter un user dans un role
	 * @param role
	 * @return
	 */
	public AddUserToRoleResponsePublikApi addUserToRole(String userUuid, String roleUuid) {

		log.info("AddUserToRole :  {} dans {} ", userUuid, roleUuid );
		// On récupère l'URL de l'api
		String purl = apiUrl + ROLES + "/{role_uuid}/members/{user_uuid}/" ;

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("role_uuid", roleUuid);
		params.put("user_uuid", userUuid);

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " role_uuid : " + roleUuid + " user_uuid : " +  userUuid);

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le role Publik
		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(null), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			AddUserToRoleResponsePublikApi autrrpa;
			try {
				autrrpa = objectMapper.readValue(response.getBody(), AddUserToRoleResponsePublikApi.class);
				return autrrpa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement de l'ajout de " + userUuid + " dans le role "+roleUuid, e);
			} 
		}
		return null;

	}

	

	

}
