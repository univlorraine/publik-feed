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
package fr.univlorraine.publikfeed.ui.view.apropos;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.service.SecurityService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class AProposView extends Div implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	@Autowired
	private transient SecurityService securityService;
	@Autowired
	private transient BuildProperties buildProperties;

	private final H4 userDivTitle = new H4();
	private final TextField username = new TextField();
	private final TextField roles = new TextField();

	@PostConstruct
	private void init() {
		getStyle().set("padding", "1em");

		initAppInfo();
		initUserInfo();
	}

	private void initAppInfo() {
		H2 nameComp = new H2(buildProperties.getName());
		nameComp.getStyle().set("display", "inline");
		add(nameComp);

		add(new Span(" v" + buildProperties.getVersion()));

		Element buildTimeElement = new Element("small");
		ZonedDateTime buildTime = buildProperties.getTime().atZone(ZoneId.systemDefault());
		buildTimeElement.setText(' ' + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(buildTime));
		buildTimeElement.getStyle().set("color", "var(--lumo-secondary-text-color)");
		getElement().appendChild(buildTimeElement);

		Paragraph descriptionComp = new Paragraph(buildProperties.get("description"));
		descriptionComp.getStyle().set("font-style", "italic");
		add(descriptionComp);
	}

	private void initUserInfo() {
		userDivTitle.getStyle().set("margin-bottom", "0");
		add(userDivTitle);

		securityService.getUsername().ifPresent(username::setValue);
		username.setReadOnly(true);

		securityService.getPrincipal()
			.map(UserDetails::getAuthorities)
			.map(authorities -> authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")))
			.ifPresent(roles::setValue);
		roles.setReadOnly(true);

		FormLayout userForm = new FormLayout(username, roles);
		add(userForm);
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("apropos.title"));

		userDivTitle.setText(getTranslation("apropos.usertitle"));
		username.setLabel(getTranslation("apropos.field.username"));
		roles.setLabel(getTranslation("apropos.field.roles"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
