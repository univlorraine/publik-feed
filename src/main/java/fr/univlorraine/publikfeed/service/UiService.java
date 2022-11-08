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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.UnicastProcessor;

@Service
@SuppressWarnings("serial")
public class UiService implements VaadinServiceInitListener {

	/* Événements UIs */
	private final UnicastProcessor<List<UiInfo>> uiInfosProcessor = UnicastProcessor.create();
	private final FluxSink<List<UiInfo>> uiInfosSink = uiInfosProcessor.sink(OverflowStrategy.LATEST);
	@Getter
	private final Flux<List<UiInfo>> uiInfosFlux = uiInfosProcessor.defaultIfEmpty(List.of()).replay(1).autoConnect();

	/* Liste d'UiInfos */
	@Getter
	private final List<UiInfo> uiInfos = Collections.synchronizedList(new ArrayList<>());

	/**
	 * UI informations.
	 */
	@Data
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	public class UiInfo {
		@EqualsAndHashCode.Include
		private int id;
		private String username;
		private String ip;
		private String location;
		private String browser;
	}

	/**
	 * @param  ui ui
	 * @return    informations sur l'ui
	 */
	private UiInfo createUiInfo(final UI ui) {
		final UiInfo uiInfo = new UiInfo();
		uiInfo.setId(System.identityHashCode(ui));
		uiInfo.setUsername(getUsernameFromUI(ui));
		uiInfo.setIp(ui.getSession().getBrowser().getAddress());
		uiInfo.setLocation(ui.getInternals().getActiveViewLocation().getPathWithQueryParameters());
		uiInfo.setBrowser(getBrowserFromUI(ui));
		return uiInfo;
	}

	/**
	 * @param  ui UI
	 * @return    nom d'utilisateur associé à l'ui
	 */
	private String getUsernameFromUI(final UI ui) {
		return Optional.ofNullable(ui)
			.map(UI::getSession)
			.map(VaadinSession::getSession)
			.map(session -> session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
			.filter(SecurityContext.class::isInstance)
			.map(SecurityContext.class::cast)
			.map(SecurityContext::getAuthentication)
			.map(Authentication::getName)
			.orElse("-");
	}

	/**
	 * @param  ui UI
	 * @return    navigateur associé à l'ui
	 */
	private String getBrowserFromUI(final UI ui) {
		return Optional.ofNullable(ui)
			.map(UI::getSession)
			.map(VaadinSession::getBrowser)
			.map(browser -> {
				final StringBuffer sb = new StringBuffer();
				if (browser.isChrome()) {
					sb.append("Chrome");
				} else if (browser.isFirefox()) {
					sb.append("Firefox");
				} else if (browser.isIE()) {
					sb.append("IE");
				} else if (browser.isEdge()) {
					sb.append("Edge");
				} else if (browser.isSafari()) {
					sb.append("Safari");
				} else if (browser.isOpera()) {
					sb.append("Opera");
				} else {
					return browser.getBrowserApplication();
				}
				sb.append(' ')
					.append(browser.getBrowserMajorVersion());
				if (browser.getBrowserMinorVersion() > 0) {
					sb.append('.')
						.append(browser.getBrowserMinorVersion());
				}
				return sb.toString();
			})
			.orElse("-");
	}

	/**
	 * @see com.vaadin.flow.server.VaadinServiceInitListener#serviceInit(com.vaadin.flow.server.ServiceInitEvent)
	 */
	@Override
	public void serviceInit(final ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiInitEvent -> registerUi(uiInitEvent.getUI()));
	}

	/**
	 * Gère une nouvelle UI.
	 * @param ui ui à enregistrer
	 */
	public void registerUi(final UI ui) {
		UiInfo uiInfo = createUiInfo(ui);
		uiInfos.add(uiInfo);

		/* Notifie l'ajout d'ui. */
		uiInfosSink.next(uiInfos);

		/* Suit les changements de vue */
		Registration trackViewChangesRegistration = ui.addAfterNavigationListener(event -> {
			uiInfo.setLocation(event.getLocation().getPathWithQueryParameters());
			uiInfosSink.next(uiInfos);
		});

		/* Désinscrit l'ui lorsqu'elle est détachée */
		ui.addDetachListener(event -> {
			trackViewChangesRegistration.remove();
			uiInfos.remove(uiInfo);
			uiInfosSink.next(uiInfos);
		});
	}

}
