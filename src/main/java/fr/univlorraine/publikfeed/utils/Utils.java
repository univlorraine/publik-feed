package fr.univlorraine.publikfeed.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.flywaydb.core.internal.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univlorraine.publikfeed.json.entity.UserJson;
import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import lombok.extern.slf4j.Slf4j;



/** @author Charlie Dubois
 *         Méthodes Utiles */
@Slf4j
public class Utils {


	/** Formate la date pour LDAP */
	/*public static String formatDateToLdap(final LocalDateTime originalDateTime) {
		if (originalDateTime == null) {
			return null;
		}
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
		return format.format(originalDateTime);
	}*/

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
			uj.setGender(2);
			if(p.getSupannCivilite()!=null && p.getSupannCivilite().contentEquals("M.")) {
				uj.setGender(1);
			}
			return uj;
		}
		return null;
	}

	

}
