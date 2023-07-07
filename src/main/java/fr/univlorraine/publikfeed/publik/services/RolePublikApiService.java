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

import java.util.HashMap;
import java.util.Map;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.ListUuidJson;
import fr.univlorraine.publikfeed.json.entity.RoleJson;
import fr.univlorraine.publikfeed.publik.entity.AddUserToRoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RoleResponsePublikApi;
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

	@Value("${publik.api.gravitee.header}")
	private transient String graviteeHeader;

	@Value("${publik.api.gravitee.apikey}")
	private transient String graviteeKey;




	/**
	 * Créer un role dans Publik
	 * @param role
	 * @return
	 */
	public RoleResponsePublikApi getRoles(String url ) {

		log.info("getAllRoles ...");

		if(!StringUtils.hasText(url)) {
			// On récupère l'URL de l'api
			url = apiUrl + ROLES + "/";
		}

		//Body
		Map<String,String> params = new HashMap<String,String>();

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + url );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le role Publik
		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			RoleResponsePublikApi rpa;
			try {
				rpa = objectMapper.readValue(response.getBody(), RoleResponsePublikApi.class);
				return rpa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement getAllRoles", e);
			} 
		}
		return null;

	}



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
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(role, graviteeHeader, graviteeKey), String.class, params);

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
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.POST, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

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




	/**
	 * Fixer les users d'un role
	 * @param role
	 * @return
	 */
	public AddUserToRoleResponsePublikApi setUsersToRole(String roleUuid, ListUuidJson users) {

		log.info("setUsersToRole :  {} ", roleUuid );
		// On récupère l'URL de l'api
		String purl = apiUrl + ROLES + "/{role_uuid}/relationships/members/" ;

		//Body
		Map<String,String> params = new HashMap<String,String>();
		params.put("role_uuid", roleUuid);

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + purl + " role_uuid : " + roleUuid );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le role Publik
		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = rt.exchange(purl, HttpMethod.PUT, Utils.createRequest(users, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response :" + response);

		//Si on a eu une réponse
		if (response != null && response.getBody()!=null) {
			ObjectMapper objectMapper = new ObjectMapper();
			AddUserToRoleResponsePublikApi autrrpa;
			try {
				autrrpa = objectMapper.readValue(response.getBody(), AddUserToRoleResponsePublikApi.class);
				return autrrpa;
			} catch (Exception e) {
				log.warn("Erreur lors du traitement qui fixe les utilisateurs du role "+roleUuid, e);
			} 
		}
		return null;

	}


	/**
	 * Supprime le role dans Publib à partir de l'uuid en parametre
	 * @param uuid
	 */
	public boolean deleteRole(String uuid) {
		log.info("Suppression role Publik {}", uuid);

		// On calcule l'URL de l'api
		String url = apiUrl + ROLES + "/" + uuid + "/";


		//Body
		Map<String,String> params = new HashMap<String,String>();

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + url );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		try {
			//Appelle du WS qui créé le role Publik
			@SuppressWarnings("unchecked")
			ResponseEntity<String> response = rt.exchange(url, HttpMethod.DELETE, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

			log.info("Publik Response " + response.getStatusCode() +" :" + response);

			//Si on a eu une réponse
			if (response.getStatusCode().equals(HttpStatus.OK) || response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
				log.info("Suppression role reponse OK : {}", response.getStatusCode());
				return true;
			}
		} catch(HttpClientErrorException hcee) {
			if(hcee != null && hcee.getStatusCode().is4xxClientError() && hcee.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				if(hcee.getResponseBodyAsString()!=null && hcee.getResponseBodyAsString().contains("Pas trouvé.")) {
					log.warn("Role non trouvé. Uuid {} not found", uuid);
					return true;
				}
			} 
			throw hcee;
		}
		return false;

	}


	/**
	 * Supprime le user du role
	 * @param userUuid
	 * @param roleId
	 */
	public boolean deleteUserFromRole(String userUuid, String roleUuid) {
		log.info("Suppression de {} du role Publik {}", userUuid, roleUuid);

		// On calcule l'URL de l'api
		String url = apiUrl + ROLES + "/" + roleUuid + "/members/" + userUuid + "/";

		//Body
		Map<String,String> params = new HashMap<String,String>();

		RestTemplate rt = new RestTemplate();

		log.info("call url : " + url );

		rt.getInterceptors().add(new BasicAuthenticationInterceptor(apiUsername, apiPassword));

		//Appelle du WS qui créé le role Publik
		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = rt.exchange(url, HttpMethod.DELETE, Utils.createRequest(null, graviteeHeader, graviteeKey), String.class, params);

		log.info("Publik Response " + response.getStatusCode() +" :" + response);


		//Si on a eu une réponse
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			log.info("Suppression du user du role reponse OK : {}", response.getStatusCode());
			return true;
		}
		return false;
	}





}
