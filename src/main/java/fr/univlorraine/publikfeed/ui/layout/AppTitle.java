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
package fr.univlorraine.publikfeed.ui.layout;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.UIScope;

import fr.univlorraine.publikfeed.service.CurrentUiService;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;

@Component
@UIScope
@SuppressWarnings("serial")
public class AppTitle extends HorizontalLayout implements LocaleChangeObserver {

	private static final String SRC_LOGO = "./images/logo.png";
	private static final String SRC_LOGO_NB = "./images/logo-nb.png";

	@Autowired
	private transient CurrentUiService currentUiService;
	@Autowired
	private transient BuildProperties buildProperties;

	private final Image logo = new Image();

	@PostConstruct
	private void init() {
		setAlignItems(Alignment.END);
		getStyle().set("margin", "0.75rem 0.75rem 0.75rem 1.5rem");

		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getDarkModeFlux()
				.map(darkMode -> darkMode ? SRC_LOGO_NB : SRC_LOGO)
				.map(logoSrc -> () -> logo.setSrc(logoSrc)));
		add(logo);

		Div appNameTitle = new Div(new Text(buildProperties.getName()));
		appNameTitle.getElement().getStyle().set("font-size", "var(--lumo-font-size-xl)");
		add(appNameTitle);
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		logo.setAlt(getTranslation("menu.alt-logo"));
	}

}
