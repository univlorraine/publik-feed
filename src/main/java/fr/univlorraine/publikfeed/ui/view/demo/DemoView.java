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
package fr.univlorraine.publikfeed.ui.view.demo;

import java.time.LocalTime;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.service.CurrentUiService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.CSSColorUtils;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class DemoView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient CurrentUiService currentUiService;
	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Checkbox darkModeCB = new Checkbox();

	private final TextField colorTF = new TextField();

	private final Button button = new Button();

	@PostConstruct
	public void init() {
		initSetBaseColor();
		initSetDarkMode();
		initTestNotif();
	}

	private void initSetBaseColor() {
		/* Pour changer la couleur de base du theme, injecter le bean
		 * currentUIService et utiliser sa méthode setAppColor([couleur CSS]).
		 *
		 * Pour changer la couleur d'une vue seulement, y ajouter un composant
		 * AppColorStyle, ex: add(new AppColorStyle("rgb(211, 47, 47)")); */
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getAppColorFlux().map(appColor -> () -> colorTF.setValue(appColor)));
		colorTF.addValueChangeListener(event -> {
			String color = event.getValue();
			if (CSSColorUtils.isSupportedColor(color)) {
				colorTF.setInvalid(false);
				currentUiService.setAppColor(color);
			} else {
				colorTF.setInvalid(true);
				colorTF.setErrorMessage(getTranslation("demo.error.invalid-color"));
			}
		});
		colorTF.setValueChangeMode(ValueChangeMode.EAGER);
		add(colorTF);
	}

	private void initSetDarkMode() {
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getDarkModeFlux().map(darkMode -> () -> darkModeCB.setValue(darkMode)));
		darkModeCB.addValueChangeListener(event -> {
			currentUiService.setDarkMode(event.getValue());
		});
		add(darkModeCB);
	}

	private void initTestNotif() {
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> notifyClicked());
		add(button);
	}

	private void notifyClicked() {
		Notification.show(getTranslation("demo.clicked", LocalTime.now()));
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("demo.title"));

		colorTF.setLabel(getTranslation("demo.color-label"));
		darkModeCB.setLabel(getTranslation("demo.dark-mode-label"));
		button.setText(getTranslation("demo.button"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
