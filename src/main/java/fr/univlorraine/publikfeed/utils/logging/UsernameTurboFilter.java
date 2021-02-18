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
