package fr.univlorraine.publikfeed.utils.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

/**
 * HttpSessionRequestCache that avoids saving internal framework requests.
 */
public class AppRequestCache extends HttpSessionRequestCache {

	/**
	 * {@inheritDoc}
	 * If the method is considered an internal request from the framework, we skip
	 * saving it.
	 * @see SecurityUtils#isFrameworkInternalRequest(HttpServletRequest)
	 */
	@Override
	public void saveRequest(final HttpServletRequest request, final HttpServletResponse response) {
		if (!SecurityUtils.isFrameworkInternalRequest(request)) {
			super.saveRequest(request, response);
		}
	}

}
