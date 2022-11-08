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
package fr.univlorraine.publikfeed.ui.view.connexions;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.service.AppUserDetailsService;
import fr.univlorraine.publikfeed.service.UiService;
import fr.univlorraine.publikfeed.service.UiService.UiInfo;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;
import lombok.Getter;

@Secured(AppUserDetailsService.ROLE_SUPERADMIN)
@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class ConnexionsView extends Grid<UiInfo> implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	@Autowired
	private transient UiService uiService;

	private final SerializableFunction<UiInfo, String> currentUIClassNameGenerator =
		uiInfo -> getUI().map(System::identityHashCode)
			.filter(uiId -> uiId == uiInfo.getId())
			.map(uiId -> "row-highlighted")
			.orElse(null);

	private final Column<UiInfo> usernameColumn = addColumn(UiInfo::getUsername)
		.setSortable(true)
		.setWidth("8rem")
		.setFlexGrow(0)
		.setClassNameGenerator(currentUIClassNameGenerator);
	private final Column<UiInfo> ipColumn = addColumn(UiInfo::getIp)
		.setSortable(true)
		.setTextAlign(ColumnTextAlign.END)
		.setWidth("9rem")
		.setFlexGrow(0)
		.setClassNameGenerator(currentUIClassNameGenerator);
	private final Column<UiInfo> browserColumn = addColumn(UiInfo::getBrowser)
		.setSortable(true)
		.setWidth("8rem")
		.setFlexGrow(0)
		.setClassNameGenerator(currentUIClassNameGenerator);
	private final Column<UiInfo> locationColumn = addColumn(UiInfo::getLocation)
		.setSortable(true)
		.setFlexGrow(1)
		.setClassNameGenerator(currentUIClassNameGenerator);

	@PostConstruct
	private void init() {
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		setSizeFull();
		setSelectionMode(SelectionMode.NONE);

		ReactiveUtils.subscribeWhenAttached(this,
			uiService.getUiInfosFlux().map(uiInfos -> () -> {
				setItems(uiInfos);
				setViewTitle(getTranslation("connexions.header", uiInfos.size()));
			}));
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("connexions.header", uiService.getUiInfos().size()));

		usernameColumn.setHeader(getTranslation("connexions.column.username"));
		ipColumn.setHeader(getTranslation("connexions.column.ip"));
		browserColumn.setHeader(getTranslation("connexions.column.browser"));
		locationColumn.setHeader(getTranslation("connexions.column.location"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
