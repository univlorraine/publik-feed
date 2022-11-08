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
package fr.univlorraine.publikfeed.ui.view.users;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.controllers.UserPublikController;
import fr.univlorraine.publikfeed.job.services.JobLauncher;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.services.RoleAutoService;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
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
public class UsersView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Resource
	private UserHisService userHisService;
	@Resource
	private JobLauncher jobLauncher;
	@Resource
	private RoleAutoService roleAutoService;
	@Resource
	private UserPublikController userPublikController;


	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Button refreshButton = new Button();
	private final Button searchErrorButton = new Button();
	private final Button importButton = new Button();
	private final TextField champRecherche = new TextField();
	
	private final Grid<UserHis> usersGrid = new Grid<>();
	private final Column<UserHis> loginColumn = usersGrid.addComponentColumn(r -> getIdAndButtonColumn(r))
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("Login");
	private final Column<UserHis> datMajColumn = usersGrid.addColumn(r -> Utils.formatDateForDisplay(r.getDatMaj()))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Maj");
	private final Column<UserHis> datSupColumn = usersGrid.addColumn(r -> Utils.formatDateForDisplay(r.getDatSup()))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Suppr");


	List<UserHis> listUsers;
	
	ListDataProvider<UserHis> dataProvider;

	@PostConstruct
	public void init() {
		initJobs();
		initGrid();
		usersGrid.setItemDetailsRenderer(new ComponentRenderer<>(r -> {
			return getDetailColumn(r);
		}));
		
		this.setHeightFull();
	}

	private Component getDetailColumn(UserHis u) {

		VerticalLayout detailLayout = new VerticalLayout();
		detailLayout.getStyle().set("margin", "0");
		detailLayout.getStyle().set("padding", "0");

		HorizontalLayout publikLayout = new HorizontalLayout();
		publikLayout.setWidthFull();

		TextField uuidField = new TextField("Uuid publik");
		uuidField.setWidthFull();
		uuidField.setValue(u.getUuid() != null ? u.getUuid() : "");
		uuidField.setReadOnly(true);
		publikLayout.add(uuidField);
		
		TextField dataField = new TextField("Data publik");
		dataField.setWidthFull();
		dataField.setValue(u.getData() != null ? u.getData() : "");
		dataField.setReadOnly(true);
		publikLayout.add(dataField);
		
		detailLayout.add(publikLayout);
		
		log.info("Recuperation des roles de {}",u.getLogin());
		List<UserRole> listeUserRole = roleAutoService.findRolesFromLogin(u.getLogin());
		HorizontalLayout roleAutoLayout = new HorizontalLayout();
		roleAutoLayout.setWidthFull();
		for(UserRole roleAuto : listeUserRole) {
			// Si le role est actif
			if(roleAuto != null) {
				VerticalLayout divRole = new VerticalLayout();
				divRole.getStyle().set("padding", "0.5em");
				divRole.getStyle().set("border-radius", "0.6em");
				divRole.setSizeUndefined();
				Label libelleRole = new Label(roleAuto.getId().getRoleId());
				divRole.add(libelleRole);
				
				Label dateMaj = new Label(Utils.formatDateForDisplay(roleAuto.getDatMaj()));
				dateMaj.getStyle().set("font-size", "smaller");
				dateMaj.getStyle().set("margin", "0");
				
				if(roleAuto.getDatSup()==null) {
					divRole.getStyle().set("background-color", "rgb(18 222 103 / 76%)");
				}else {
					dateMaj.setText(Utils.formatDateForDisplay(roleAuto.getDatSup()));
					divRole.getStyle().set("background-color", "#cc2929");
				}
				divRole.add(dateMaj);
				roleAutoLayout.add(divRole);
			}
		}
		detailLayout.add(roleAutoLayout);
		
		Button deleteButton = new Button("Supprimer de Publik");
		deleteButton.setIcon(VaadinIcon.TRASH.create());
		deleteButton.addClickListener(e-> {
			if(userPublikController.suppressionUser(u)) {
				Optional<UserHis> uh=userHisService.find(u.getLogin());
				usersGrid.getDataProvider().refreshItem(uh.get());
				Notification.show(getTranslation("users.suppr.ok.notif", LocalTime.now()));
			}else {
				Notification.show(getTranslation("users.suppr.ko.notif", LocalTime.now()));
			}	
		});
		deleteButton.setVisible(u.getDatSup()==null);
		detailLayout.add(deleteButton);
		
		
		Button addButton = new Button("Créer dans Publik");
		addButton.setIcon(VaadinIcon.ADD_DOCK.create());
		addButton.addClickListener(e-> {
			if(userPublikController.createOrUpdateUser(u.getLogin())) {
				Optional<UserHis> uh=userHisService.find(u.getLogin());
				usersGrid.getDataProvider().refreshItem(uh.get());
				Notification.show(getTranslation("users.add.ok.notif", LocalTime.now()));
			}else {
				Notification.show(getTranslation("users.add.ko.notif", LocalTime.now()));
			}	
		});
		addButton.setVisible(u.getDatSup()!=null);
		detailLayout.add(addButton);

		return detailLayout;
	}

	private void initGrid() {

		usersGrid.setHeightFull();
		usersGrid.setSelectionMode(SelectionMode.NONE);
		usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		usersGrid.setPageSize(40);

		updateUsers(null);

		add(usersGrid);
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
			if(StringUtils.isBlank(champRecherche.getValue())) {
				searchErrorButton.setVisible(false);
			} else {
				searchErrorButton.setVisible(true);
			}
			updateUsers(champRecherche.getValue());
		});
		buttonsLayout.add(champRecherche);
		
		
		searchErrorButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchErrorButton.setIcon(VaadinIcon.AMBULANCE.create());
		searchErrorButton.addClickListener(event -> searchErrorUser(champRecherche.getValue()));
		searchErrorButton.setVisible(false);
		buttonsLayout.add(searchErrorButton);
		
		importButton.setText("Importer");
		importButton.setIcon(VaadinIcon.PLUS.create());
		importButton.setVisible(false);
		importButton.addClickListener(e-> {
			userPublikController.createOrUpdateUser(champRecherche.getValue());
			updateUsers(champRecherche.getValue());
		});
		buttonsLayout.add(importButton);
		
		add(buttonsLayout);
	}
	
	private void searchErrorUser(String value) {
		log.info("Search UserErrHis : {}", value);
		UI.getCurrent().navigate("usererrhis/" + value);
	}
	
	private Component getIdAndButtonColumn(UserHis u) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setAlignItems(Alignment.CENTER);
		Button details = new Button(usersGrid.isDetailsVisible(u) ? VaadinIcon.ANGLE_UP.create() : VaadinIcon.ANGLE_DOWN.create(),
			e -> usersGrid.setDetailsVisible(u, !usersGrid.isDetailsVisible(u)));
		Label label = new Label(u.getLogin());
		hl.add(details);
		hl.addAndExpand(label);
		return hl;
	}

	private void updateUsers(String search) {
		if(StringUtils.isBlank(search)) {
			listUsers = userHisService.findAll();
		} else {
			listUsers = userHisService.findFor(search);
		}
		if(listUsers.isEmpty()) {
			importButton.setVisible(true);
			importButton.setText("Importer "+search);
		}else {
			importButton.setVisible(false);
			importButton.setText("Importer");
		}
		dataProvider = DataProvider.ofCollection(listUsers);
		usersGrid.setDataProvider(dataProvider);
	}


	private void notifyClicked() {
		Notification.show(getTranslation("users.clicked", LocalTime.now()));
		updateUsers(champRecherche.getValue());
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("users.title"));

		refreshButton.setText(getTranslation("users.refresh"));
		searchErrorButton.setText(getTranslation("users.searcherror"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
