package fr.univlorraine.publikfeed.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import com.vaadin.flow.i18n.I18NProvider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service de messages I18N.
 * @author Adrien Colson
 */
@Service
@Slf4j
@SuppressWarnings("serial")
public class I18NProviderService implements I18NProvider {

	@Autowired
	private transient MessageSource messageSource;

	@Getter
	private final List<Locale> providedLocales = List.of(Locale.FRANCE);

	/**
	 * @see com.vaadin.flow.i18n.I18NProvider#getTranslation(java.lang.String, java.util.Locale, java.lang.Object[])
	 */
	@Override
	public String getTranslation(final String key, final Locale locale, final Object... params) {
		Object[] convertedParams = Stream.of(params)
			.map(param -> {
				if (param instanceof LocalDateTime) {
					return Date.from(((LocalDateTime) param).atZone(ZoneId.systemDefault()).toInstant());
				} else if (param instanceof LocalDate) {
					return Date.from(((LocalDate) param).atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());
				} else if (param instanceof LocalTime) {
					return Date.from(((LocalTime) param).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
				} else {
					return param;
				}
			})
			.toArray();
		try {
			return messageSource.getMessage(key, convertedParams, locale);
		} catch (NoSuchMessageException e) {
			log.error("La clé i18n '{}' est manquante.", key);
			return "!{" + key + "}";
		}
	}

}
