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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.UnicastProcessor;

@UIScope
@Service
public class CurrentUiService {

	@Value("${app.color}")
	private transient String appColorDefault;

	@PostConstruct
	private void init() {
		setAppColor(appColorDefault);
		setDarkModeFromMedia();
	}

	/* Theme : Couleur principale */

	private final UnicastProcessor<String> appColorProcessor = UnicastProcessor.create();
	private final FluxSink<String> appColorSink = appColorProcessor.sink(OverflowStrategy.LATEST);
	@Getter
	private final Flux<String> appColorFlux = appColorProcessor.replay(1).autoConnect();

	public void setAppColor(final String value) {
		appColorSink.next(value);
	}

	/* Theme : Mode sombre */

	private final UnicastProcessor<Boolean> darkModeProcessor = UnicastProcessor.create();
	private final FluxSink<Boolean> darkModeSink = darkModeProcessor.sink(OverflowStrategy.LATEST);
	@Getter
	private final Flux<Boolean> darkModeFlux = darkModeProcessor.replay(1).autoConnect();

	public void setDarkMode(final Boolean value) {
		darkModeSink.next(value);
	}

	/**
	 * Détecte la préférence du mode sombre.
	 */
	public void setDarkModeFromMedia() {
		UI.getCurrent()
			.getPage()
			.executeJs("return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches")
			.then(Boolean.class, this::setDarkMode);
	}

}
