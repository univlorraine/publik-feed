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
package fr.univlorraine.publikfeed.test.integration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.support.WebTestUtils;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.GenericFilterBean;

import fr.univlorraine.publikfeed.model.app.entity.Utilisateur;

/**
 * Permet de simuler l'authentification d'un utilisateur via l'url à des fins de tests.
 */
@TestComponent
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestParameterUserFilter extends GenericFilterBean {

	/** username parameter. */
	public static final String USERNAME_PARAMETER = "username";
	/** authority parameter. */
	public static final String AUTHORITY_PARAMETER = "authority";

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		final String username = servletRequest.getParameter(USERNAME_PARAMETER);
		if (username != null && !username.isEmpty()) {
			final SecurityContextRepository securityContextRepository = WebTestUtils.getSecurityContextRepository(servletRequest);
			final HttpRequestResponseHolder requestResponseHolder = new HttpRequestResponseHolder(servletRequest, servletResponse);
			securityContextRepository.loadContext(requestResponseHolder);
			servletRequest = requestResponseHolder.getRequest();
			servletResponse = requestResponseHolder.getResponse();

			final String[] authorities = servletRequest.getParameterValues(AUTHORITY_PARAMETER);
			final SecurityContext securityContext = createSecurityContext(username, authorities);
			SecurityContextHolder.setContext(securityContext);
			securityContextRepository.saveContext(securityContext, servletRequest, servletResponse);
		}

		chain.doFilter(request, response);
	}

	/**
	 * @param  username    username
	 * @param  authorities roles
	 * @return             security context
	 */
	private SecurityContext createSecurityContext(final String username, final String... authorities) {
		final Collection<GrantedAuthority> grantedAuthorities = authorities == null ? List.of() : AuthorityUtils.createAuthorityList(authorities);
		Utilisateur principal = new Utilisateur();
		principal.setUsername(username);
		principal.getAuthorities().addAll(grantedAuthorities);
		final Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());

		final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		return securityContext;
	}

}
