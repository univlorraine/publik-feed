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
