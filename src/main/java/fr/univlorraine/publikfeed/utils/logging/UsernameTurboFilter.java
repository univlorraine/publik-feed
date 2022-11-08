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
package fr.univlorraine.publikfeed.utils.logging;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Ajoute le nom d'utilisateur au MDC.
 * @author Adrien Colson
 */
public class UsernameTurboFilter extends TurboFilter {

	/** user_key. */
	private static final String USER_KEY = "username";

	/**
	 * @see ch.qos.logback.classic.turbo.TurboFilter#decide(org.slf4j.Marker, ch.qos.logback.classic.Logger, ch.qos.logback.classic.Level, java.lang.String,
	 *      java.lang.Object[], java.lang.Throwable)
	 */
	@Override
	public FilterReply
		decide(final Marker marker, final Logger logger, final Level level, final String format, final Object[] params, final Throwable throwable) {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !registerUsername(auth.getName())) {
			MDC.remove(USER_KEY);
		}

		return FilterReply.NEUTRAL;
	}

	/**
	 * Register the user in the MDC under USER_KEY.
	 * @param  username user name
	 * @return          true id the user can be successfully registered
	 */
	private boolean registerUsername(final String username) {
		if (username != null && !username.isBlank()) {
			MDC.put(USER_KEY, username);
			return true;
		}
		return false;
	}

}
