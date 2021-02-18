package fr.univlorraine.publikfeed.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import fr.univlorraine.publikfeed.model.app.entity.Utilisateur;
import fr.univlorraine.publikfeed.ui.view.error.AccessDeniedView;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings("serial")
public class SecurityService implements VaadinServiceInitListener {

	@Autowired
	private transient BeanFactory beanFactory;

	private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
	private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

	@PostConstruct
	private void init() {
		evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
	}

	/**
	 * @see com.vaadin.flow.server.VaadinServiceInitListener#serviceInit(com.vaadin.flow.server.ServiceInitEvent)
	 */
	@Override
	public void serviceInit(final ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().addBeforeEnterListener(beforeEvent -> {
			if (!isAccessGranted(beforeEvent.getNavigationTarget())) {
				beforeEvent.rerouteToError(new AccessDeniedException(""), "");
			}
		}));
	}

	public Optional<SecurityContext> getSecurityContext() {
		return Optional.of(SecurityContextHolder.getContext());
	}

	public Optional<Authentication> getAuthentication() {
		return getSecurityContext().map(SecurityContext::getAuthentication);
	}

	public Optional<String> getUsername() {
		return getAuthentication().map(Authentication::getName);
	}

	public Optional<Utilisateur> getPrincipal() {
		return getAuthentication().map(Authentication::getPrincipal)
			.map(Utilisateur.class::cast);
	}

	public boolean isUserLoggedIn() {
		return getAuthentication()
			.filter(Predicate.not(AnonymousAuthenticationToken.class::isInstance))
			.map(Authentication::isAuthenticated)
			.orElse(false);
	}

	public boolean isAccessGranted(final Class<?> securedClass) {
		return (AccessDeniedView.class.equals(securedClass) || isUserLoggedIn())
			&& isAccessGrantedForPreAuthorize(securedClass)
			&& isAccessGrantedForRoleAnnotations(securedClass);
	}

	private boolean isAccessGrantedForPreAuthorize(final Class<?> securedClass) {
		PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(securedClass, PreAuthorize.class);
		if (preAuthorize == null) {
			return true;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}

		SecurityExpressionRoot securityExpressionRoot = new SecurityExpressionRoot(authentication) {
		};

		try {
			return spelExpressionParser.parseExpression(preAuthorize.value()).getValue(evaluationContext, securityExpressionRoot, Boolean.class);
		} catch (EvaluationException | ParseException e) {
			log.error("Erreur lors de la pré-autorisation d'accès à {}.", securedClass, e);
			return false;
		}
	}

	private boolean isAccessGrantedForRoleAnnotations(final Class<?> securedClass) {
		final Set<String> allowedRoles = Stream.of(
			Optional.ofNullable(AnnotationUtils.findAnnotation(securedClass, Secured.class))
				.map(Secured::value),
			Optional.ofNullable(AnnotationUtils.findAnnotation(securedClass, RolesAllowed.class))
				.map(RolesAllowed::value))
			.filter(Predicate.not(Optional::isEmpty))
			.map(Optional::get)
			.flatMap(Arrays::stream)
			.collect(Collectors.toSet());

		if (allowedRoles.isEmpty()) {
			return true;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}

		return authentication
			.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch(allowedRoles::contains);
	}

}
