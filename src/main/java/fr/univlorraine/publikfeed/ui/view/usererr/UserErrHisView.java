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
package fr.univlorraine.publikfeed.ui.view.usererr;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.job.services.JobLauncher;
import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;
import fr.univlorraine.publikfeed.model.app.services.RoleAutoService;
import fr.univlorraine.publikfeed.model.app.services.UserErrHisService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
@Slf4j
public class UserErrHisView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver,  HasUrlParameter<String> {

	@Resource
	private UserErrHisService userErrHisService;
	@Resource
	private JobLauncher jobLauncher;
	@Resource
	private RoleAutoService roleAutoService;

	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Button refreshButton = new Button();
	private final TextField champRecherche = new TextField();
	
	private final Grid<UserErrHis> errorGrid = new Grid<>();
	private final Column<UserErrHis> loginColumn = errorGrid.addColumn(r -> r.getLogin())
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("Login");
	private final Column<UserErrHis> datMajColumn = errorGrid.addColumn(r -> Utils.formatDateForDisplay(r.getDatErr()))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Erreur");
	private final Column<UserErrHis> datSupColumn = errorGrid.addColumn(r -> r.getTrace())
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Erreur");


	List<UserErrHis> listUsers;
	
	ListDataProvider<UserErrHis> dataProvider;

	@PostConstruct
	public void init() {
		initJobs();
		initGrid();
		
		this.setHeightFull();
	}


	private void initGrid() {

		errorGrid.setHeightFull();
		errorGrid.setSelectionMode(SelectionMode.NONE);
		errorGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		errorGrid.setPageSize(40);

		updateUserErrHis(null);

		add(errorGrid);
	}

	private void initJobs() {
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		refreshButton.addClickListener(event -> notifyClicked());
		buttonsLayout.add(refreshButton);
		
		champRecherche.setAutofocus(true);
		champRecherche.setWidth("300px");
		champRecherche.setClearButtonVisible(true);
		champRecherche.addValueChangeListener( e -> {
			updateUserErrHis(champRecherche.getValue());
		});
		buttonsLayout.add(champRecherche);
		
		add(buttonsLayout);
	}



	private void updateUserErrHis(String search) {
		if(StringUtils.isBlank(search)) {
			listUsers = userErrHisService.findAll();
		} else {
			listUsers = userErrHisService.findFor(search);
		}
		dataProvider = DataProvider.ofCollection(listUsers);
		errorGrid.setDataProvider(dataProvider);
	}


	private void notifyClicked() {
		Notification.show(getTranslation("errorusers.clicked", LocalTime.now()));
		updateUserErrHis(champRecherche.getValue());
	}
	
	@Override
	public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String value) {
		if(!StringUtils.isBlank(value)) {
			champRecherche.setValue(value);
		}
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("errorusers.title"));

		refreshButton.setText(getTranslation("errorusers.refresh"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
