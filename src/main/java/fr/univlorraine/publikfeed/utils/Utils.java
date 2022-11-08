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
package fr.univlorraine.publikfeed.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.ListUuidJson;
import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.json.entity.UuidJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import lombok.extern.slf4j.Slf4j;



/** @author Charlie Dubois
 *         Méthodes Utiles */
@Slf4j
public class Utils {

	public static final String PREFIX_ROLE_UNITAIRE = "ZZU_";

	public static final String PREFIX_ROLE_NOMINATIF = "RN_";

	public static final String PREFIX_ROLE_PERSONNEL = "EMP";

	public static final String PREFIX_ROLE_ETUDIANT = "ETU";
	
	public static final String PREFIX_ROLE_AUTRES = "AUTRES";

	public static final String ROLE_SEPARATOR = "_";

	public static final String PREFIX_ROLE_BC = "BC";

	public static final String PREFIX_ROLE_MANUEL = "ZZM_";
	
	public static final String PREFIX_ROLE_RESP = "ZZR_";
	
	public static final String EPPN_SUFFIX = "@univ-lorraine.fr";
	
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).localizedBy(Locale.FRANCE);

	public static final CharSequence ANOMALIE_PUBLIK_MAIL = "courriel est invalide";



	



	/** Formate la date pour LDAP */
	/*public static String formatDateToLdap(final LocalDateTime originalDateTime) {
		if (originalDateTime == null) {
			return null;
		}
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
		return format.format(originalDateTime);
	}*/
	
	/** Creation d'une date à partir d'une date au format LDAP RFC822 */
	public static LocalDateTime getDateFromLdap(String ldaptimestamp) {
		if(StringUtils.hasText(ldaptimestamp)) {

			// suppression du Z de fin de date ldap
			ldaptimestamp = ldaptimestamp.replaceAll("Z", "");
			
			DateFormat gmtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			TimeZone gmtTime = TimeZone.getTimeZone("GMT");
			gmtFormat.setTimeZone(gmtTime);
			
			LocalDateTime ldt;
			try {
				ldt = new java.sql.Timestamp(gmtFormat.parse(ldaptimestamp).getTime()).toLocalDateTime();
				log.debug("Date ldap {} -> localDateTime : {}", ldaptimestamp, ldt);
				return ldt;
			} catch (ParseException e) {
				log.error("Erreur de parsing de date ",e);
			}
		}
		return null;
	}

	/** Formatage d'une date au format LDAP RFC822 */
	public static String formatDateToLdap(final LocalDateTime originalDateTime) {
		if (originalDateTime == null) {
			return "19000101010101Z";
		}
		DateFormat gmtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtFormat.setTimeZone(gmtTime);
		String resultDateTime = gmtFormat.format(java.sql.Timestamp.valueOf(originalDateTime));
		resultDateTime += "Z";
		log.info("{} -> {}", originalDateTime, resultDateTime);
		return resultDateTime;
	}
	
	public static String formatDateForPublik(LocalDateTime date) {
		if (date == null) {
			return null;
		}
		DateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtFormat.setTimeZone(gmtTime);
		String resultDateTime = gmtFormat.format(java.sql.Timestamp.valueOf(date));
		resultDateTime= resultDateTime.replaceFirst(" ", "T");
		log.info("{} -> {}", date, resultDateTime);
		return resultDateTime;
	}


	public static UserJson getUserJson(UserHis user) {
		if(user != null && StringUtils.hasText(user.getData())) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readValue(user.getData(), UserJson.class);
			} catch (Exception e) {
				log.warn("Exception lors de la conversion json des data du userHis "+user.getLogin(),e);
				return null;
			} 	
		}
		return null;
	}

	public static UserJson getUserJson(PeopleLdap p) {
		if(p != null) {
			UserJson uj = new UserJson();
			uj.setUsername(p.getEduPersonPrincipalName());
			uj.setFirstName(p.getGivenName());
			uj.setLastName(p.getSn());
			uj.setEmail(p.getMail());
			/*uj.setGender(2);
			if(p.getSupannCivilite()!=null && p.getSupannCivilite().contentEquals("M.")) {
				uj.setGender(1);
			}*/
			return uj;
		}
		return null;
	}


	public static String formatDateForDisplay(LocalDateTime date) {
		if(date !=null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			return date.format(formatter);
		}
		return null;
	}


	public static HttpEntity createRequest(Object body, String apiHeader, String apiKey) {
		// Headers
		HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON.toString());
		
		// Ajout ApiKey si nécessaire
		if(StringUtils.hasText(apiHeader) && StringUtils.hasText(apiKey)) {
			requestHeaders.add(apiHeader, apiKey);
		}

		// Request
		if(body ==null) {
			return new HttpEntity<>(requestHeaders);
		}
		return new HttpEntity<>(body , requestHeaders);
	}

	public static HttpHeaders createHeaders(String contentType) {
		HttpHeaders requestHeaders =new HttpHeaders();
		//requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("Content-Type", contentType);

		//Si on a une api key pour appeler les WS
		/*if(PropertyUtils.getWsSihamApiKey()!=null) {
			requestHeaders.add(Utils.GRAVITEE_KEY_HEADER, PropertyUtils.getWsSihamApiKey());
		}*/
		return requestHeaders;
	}

	/*
	 * 
	 * Retourne l'objet ListUuidJson correspondans à la liste uuids en parametre
	 */
	public static ListUuidJson getListUuidJson(List<String> uuids) {
		ListUuidJson data = new ListUuidJson();
		List<UuidJson> liste = new LinkedList<UuidJson> ();
		
		// Ajout des uuids dans la liste
		for(String uuid : uuids) {
			UuidJson j = new UuidJson();
			j.setUuid(uuid);
			liste.add(j);
		}
		// Ajout de la liste à l'objet json
		data.setData(liste);
		return data;
	}

	public static String getHash(ListUuidJson data) {
		String hash = null;
		// Si il y a des uuids dans le role
		if(!data.getData().isEmpty()) {
			//  Creation du hash à partir de l'objet
			hash = String.valueOf(data.hashCode());
		}
		return hash;
	}

	public static List<String> listPeopleToListLogin(List<PeopleLdap> lp) {
		List<String> list = new LinkedList<String> ();
		for(PeopleLdap p : lp) {
			list.add(p.getUid());
		}
		return list;
	}

	// Retourne vrai si le compte n'est pas un compte étudiant.
	public static boolean isNotStudent(PeopleLdap p) {
		return !StringUtils.hasText(p.getSupannEtuId()) || StringUtils.hasText(p.getSupannEmpId());
	}


	


}
