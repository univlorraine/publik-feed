package fr.univlorraine.publikfeed.ui.view.rolemanuel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.patterns.IfPointcut.IfFalsePointcut;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;

import fr.univlorraine.publikfeed.job.services.JobLauncher;
import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.services.RoleManuelService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Route(layout = MainLayout.class)
@Slf4j
@SuppressWarnings("serial")
public class RoleManuelView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Resource
	private RoleManuelService roleManuelService;
	@Resource
	private JobLauncher jobLauncher;

	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Button button = new Button();
	FormLayout addLayout = new FormLayout();
	private final Button buttonNew = new Button();
	private final Button buttonCancel= new Button();
	private final Button buttonCreate= new Button();
	private final TextField champRecherche = new TextField();
	private final Button buttonCsv = new Button();

	TextField idField = new TextField();
	TextField libField = new TextField();
	TextField filtreField = new TextField();
	TextField loginsField = new TextField();

	private final Grid<RoleManuel> rolesGrid = new Grid<>();
	private final Column<RoleManuel> codeColumn = rolesGrid.addComponentColumn(r -> getIdAndButtonColumn(r))
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("ID");
	private final Column<RoleManuel> libColumn = rolesGrid.addComponentColumn(r -> getLibelleColumn(r))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Libellé");
	private final Column<RoleManuel> selectorColumn = rolesGrid.addComponentColumn(r -> getStateColumn(r))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Etat");



	List<RoleManuel> listRoles;

	ListDataProvider<RoleManuel> dataProvider;

	@PostConstruct
	public void init() {
		initJobs();
		initAddForm();
		initGrid();
		rolesGrid.setItemDetailsRenderer(new ComponentRenderer<>(r -> {
			return getDetailColumn(r);
		}));
		this.setHeightFull();
	}


	private Component getIdAndButtonColumn(RoleManuel r) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setAlignItems(Alignment.CENTER);
		Button details = new Button(rolesGrid.isDetailsVisible(r) ? VaadinIcon.MINUS.create() : VaadinIcon.PLUS.create(),
			e -> rolesGrid.setDetailsVisible(r, !rolesGrid.isDetailsVisible(r)));
		Label label = new Label(r.getId());
		hl.add(details);
		hl.addAndExpand(label);
		return hl;
	}

	private Component getLibelleColumn(RoleManuel r) {
		HorizontalLayout libelleLayout = new HorizontalLayout();
		libelleLayout.setWidthFull();
		TextField libelleField = new TextField();
		libelleField.setWidthFull();
		libelleField.setValue(r.getLibelle());
		libelleField.setReadOnly(true);
		libelleLayout.add(libelleField);

		Button validButton = new Button();
		validButton.setIcon(VaadinIcon.CHECK.create());
		validButton.setVisible(false);

		Button editButton = new Button();
		editButton.setIcon(VaadinIcon.PENCIL.create());
		editButton.addClickListener(e -> {
			libelleField.setReadOnly(false);
			editButton.setVisible(false);
			validButton.setVisible(true);
		});

		validButton.addClickListener(e -> {
			try {
				RoleManuel updatedRole = roleManuelService.updateLibelle(r, libelleField.getValue());
				rolesGrid.getDataProvider().refreshItem(updatedRole);
				Notification.show(getTranslation("maj.ok.notif", LocalTime.now()));
			} catch(Exception ex) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.show(getTranslation("erreur.notif") + " : "+ ex.getMessage());
				log.error("Erreur lors de la maj du libelle du role "+r.getId(), ex);
			}
		});
		libelleLayout.add(validButton);
		libelleLayout.add(editButton);
		return libelleLayout;

	}
	private Component getStateColumn(RoleManuel r) {
		// Si le role n'est pas encore créé dans publik
		if(r.getDatMaj()!=null && r.getDatSup()==null && r.getDatCrePublik()==null) {
			return VaadinIcon.CLOCK.create();
		}
		// Si la date maj du role est superieur a la datemaj publik
		if(r.getDatMaj()!=null && r.getDatSup()==null && r.getDatCrePublik()!=null && r.getDatCrePublik().isBefore(r.getDatMaj())
			&& (r.getDatMaj()==null || r.getDatMaj().isBefore(r.getDatMaj()))) {
			return VaadinIcon.CLOCK.create();
		}
		// Si le role n'est pas encore créé dans publik
		if(r.getDatMaj()!=null && r.getDatSup()==null && r.getDatCrePublik()==null) {
			return VaadinIcon.CLOCK.create();
		}
		// Si le role est supprimé dans la base mais pas encore dans publik
		if(r.getDatSup()!=null && r.getDatCrePublik()!=null && r.getDatSupPublik()==null) {
			return VaadinIcon.CLOCK.create();
		}
		// Si le role est supprimé dans la base et n'existe pas dans publik
		if(r.getDatSup()!=null && (r.getDatCrePublik()==null || r.getDatSupPublik()!=null)) {
			return VaadinIcon.TRASH.create();
		}
		return VaadinIcon.CHECK.create();
	}
	private Component getDetailColumn(RoleManuel r) {

		VerticalLayout detailLayout = new VerticalLayout();
		detailLayout.getStyle().set("margin", "0");
		detailLayout.getStyle().set("padding", "0");

		HorizontalLayout filtreEtLoginlayout = new HorizontalLayout();
		filtreEtLoginlayout.setWidthFull();
		TextField filtreField = new TextField("Filtre LDAP");
		filtreField.setWidthFull();
		filtreField.setValue(r.getFiltre() != null ? r.getFiltre() : "");
		filtreField.setReadOnly(true);
		filtreEtLoginlayout.add(filtreField);
		TextField loginsField = new TextField("Logins");
		loginsField.setWidthFull();
		loginsField.setValue(r.getLogins() != null ? r.getLogins() : "");
		loginsField.setReadOnly(true);
		filtreEtLoginlayout.add(loginsField);
		TextField dateMajField = new TextField("Date Maj");
		dateMajField.setValue(r.getDatMaj() != null ? Utils.formatDateForDisplay(r.getDatMaj()) : "");
		dateMajField.setReadOnly(true);
		filtreEtLoginlayout.add(dateMajField);
		TextField dateSupField = new TextField("Date Suppression");
		dateSupField.setValue(r.getDatSup() != null ? Utils.formatDateForDisplay(r.getDatSup()) : "");
		dateSupField.setReadOnly(true);
		filtreEtLoginlayout.add(dateSupField);
		detailLayout.add(filtreEtLoginlayout);



		HorizontalLayout publikLayout = new HorizontalLayout();
		publikLayout.setWidthFull();

		TextField uuidField = new TextField("Uuid publik");
		uuidField.setWidthFull();
		uuidField.setValue(r.getUuid() != null ? r.getUuid() : "");
		uuidField.setReadOnly(true);
		publikLayout.add(uuidField);

		TextField dateCrePublikField = new TextField("Date Creation Publik");
		dateCrePublikField.setValue(r.getDatCrePublik() != null ? Utils.formatDateForDisplay(r.getDatCrePublik()) : "");
		dateCrePublikField.setReadOnly(true);
		publikLayout.add(dateCrePublikField);

		TextField dateMajPublikField = new TextField("Date Maj Publik");
		dateMajPublikField.setValue(r.getDatMajPublik() != null ? Utils.formatDateForDisplay(r.getDatMajPublik()) : "");
		dateMajPublikField.setReadOnly(true);
		publikLayout.add(dateMajPublikField);

		TextField dateSupPublikField = new TextField("Date Suppression Publik");
		dateSupPublikField.setValue(r.getDatSupPublik() != null ? Utils.formatDateForDisplay(r.getDatSupPublik()) : "");
		dateSupPublikField.setReadOnly(true);
		publikLayout.add(dateSupPublikField);

		Button validButton = new Button();
		validButton.setText("Valider");
		validButton.setIcon(VaadinIcon.CHECK.create());
		validButton.setVisible(false);

		Checkbox checkBox = new Checkbox(); 
		checkBox.setLabel("Actif");
		checkBox.setValue(r.getDatSup() == null);
		checkBox.setVisible(false);

		Button editButton = new Button();
		editButton.setText("Editer");
		editButton.setIcon(VaadinIcon.PENCIL.create());
		editButton.addClickListener(e -> {
			filtreField.setReadOnly(false);
			loginsField.setReadOnly(false);
			editButton.setVisible(false);
			validButton.setVisible(true);
			checkBox.setVisible(true);
		});



		validButton.addClickListener(e -> {
			try {
				RoleManuel updatedRole = roleManuelService.updateFiltreAndLogins(r, filtreField.getValue(), loginsField.getValue(), checkBox.getValue());
				rolesGrid.getDataProvider().refreshItem(updatedRole);
				Notification.show(getTranslation("maj.ok.notif", LocalTime.now()));


			} catch(Exception ex) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.show(getTranslation("erreur.notif") + " : "+ ex.getMessage());
				log.error("Erreur lors de la maj du role "+r.getId(), ex);
			}
		});

		detailLayout.add(publikLayout);

		HorizontalLayout editBtnLayout = new HorizontalLayout();
		editBtnLayout.add(editButton);
		editBtnLayout.add(validButton);
		editBtnLayout.add(checkBox);
		detailLayout.add(editBtnLayout);

		return detailLayout;
	}

	private void initAddForm() {

		idField.setLabel("ID");
		idField.setPlaceholder("Identifiant");
		idField.setRequired(true);

		libField.setLabel("Libellé");
		libField.setPlaceholder("Description");
		libField.setRequired(true);

		filtreField.setLabel("Filtre");
		filtreField.setPlaceholder("filtre ldap");

		loginsField.setLabel("Logins");
		loginsField.setPlaceholder("liste de logins séparés par une virgule");

		idField.addValueChangeListener(e-> {
			if(idField.getValue()!=null) {
				idField.setValue(idField.getValue().toUpperCase());
			}
			majFormData();
		});
		libField.addValueChangeListener(e-> {
			majFormData();
		});
		filtreField.addValueChangeListener(e-> {
			majFormData();
		});
		loginsField.addValueChangeListener(e-> {
			majFormData();
		});

		addLayout.add(idField, libField, filtreField, loginsField);
		addLayout.setVisible(false);

		add(addLayout);
	}

	private void majFormData() {
		if(formHasAllRequiredValues()) {
			buttonCreate.setEnabled(true);
		} else {
			buttonCreate.setEnabled(false);
		}

	}


	private boolean formHasAllRequiredValues() {
		return (!StringUtils.isBlank(idField.getValue())
			&& !StringUtils.isBlank(libField.getValue()) 
			&& (!StringUtils.isBlank(filtreField.getValue()) 
				|| !StringUtils.isBlank(loginsField.getValue()) ));
	}


	private void initGrid() {

		rolesGrid.setHeightFull();
		rolesGrid.setSelectionMode(SelectionMode.NONE);
		rolesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		rolesGrid.setPageSize(40);
		
		updateRole(null);

		add(rolesGrid);
	}

	private void initJobs() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> notifyClicked());
		buttonsLayout.add(button);

		buttonCsv.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonCsv.addClickListener(event -> uploadCsv());
		// On masque le bouton pour l'instant
		buttonCsv.setVisible(false);
		buttonsLayout.add(buttonCsv);

		champRecherche.setAutofocus(true);
		champRecherche.setWidth("300px");
		champRecherche.setClearButtonVisible(true);
		champRecherche.addValueChangeListener( e -> {
			updateRole(champRecherche.getValue());
		});
		buttonsLayout.add(champRecherche);

		buttonNew.setText("Nouveau rôle");
		buttonNew.setIcon(VaadinIcon.PLUS.create());
		buttonNew.addClickListener(event -> {
			addLayout.setVisible(true);
			buttonCancel.setVisible(true);
			buttonCreate.setVisible(true);
			buttonCreate.setEnabled(false);
			buttonNew.setVisible(false);
		});
		buttonsLayout.add(buttonNew);

		buttonCancel.setText("Annuler");
		buttonCancel.setIcon(VaadinIcon.ARROW_BACKWARD.create());
		buttonCancel.addClickListener(event -> {
			addLayout.setVisible(false);
			buttonCancel.setVisible(false);
			buttonCreate.setVisible(false);
			buttonNew.setVisible(true);
		});
		buttonCancel.setVisible(false);
		buttonsLayout.add(buttonCancel);

		buttonCreate.setText("Créer le rôle");
		buttonCreate.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		buttonCreate.setIcon(VaadinIcon.CHECK.create());
		buttonCreate.addClickListener(event -> {
			if(formHasAllRequiredValues()) {
				RoleManuel role = creerRoleManuel();
				if(role!=null) {
					Notification.show("Création effectuée");
					addLayout.setVisible(false);
					buttonCancel.setVisible(false);
					buttonCreate.setVisible(false);
					buttonNew.setVisible(true);
					updateRole(champRecherche.getValue());
				} else {
					Notification.show("La création a échouée");
				}
			} else {
				Notification.show("Certaines valeurs sont manquantes.");
			}

		});
		buttonCreate.setVisible(false);
		buttonsLayout.add(buttonCreate);

		add(buttonsLayout);
	}

	private RoleManuel creerRoleManuel() {
		boolean ko = false;
		if(StringUtils.isBlank(idField.getValue()) || !idField.getValue().matches("[0-9A-Z][0-9A-Z_]*[0-9A-Z]")) {
			Notification.show("L'id n'est pas valide");
			ko = true;
		}
		if(!StringUtils.isBlank(filtreField.getValue()) && filtreField.getValue().contains(" ")) {
			Notification.show("Le filtre ne doit pas contenir d'espace");
			ko = true;
		}
		if(!StringUtils.isBlank(loginsField.getValue()) && loginsField.getValue().contains(" ")) {
			Notification.show("La liste de login ne doit pas contenir d'espace");
			ko = true;
		}
		if(ko) {
			return null;
		}
		
		// creer le role et refreshAll
		RoleManuel newRole = new RoleManuel();
		newRole.setId(idField.getValue());
		newRole.setLibelle(libField.getValue());
		if(!StringUtils.isBlank(filtreField.getValue())) {
			newRole.setFiltre(filtreField.getValue());
		}
		if(!StringUtils.isBlank(loginsField.getValue())) {
			newRole.setLogins(loginsField.getValue());
		}
		newRole.setDatMaj(LocalDateTime.now());
		newRole = roleManuelService.saveRole(newRole);
		
		// reset formulaire si création ok
		idField.clear();
		libField.clear();
		filtreField.clear();
		loginsField.clear();
		
		return newRole;
		
		
	}


	private RoleManuel addRole() {
		// TODO popup ajout role
		return null;
	}


	private void uploadCsv() {
		Notification.show(getTranslation("rolemanuel.clicked", LocalTime.now()));
	}

	private void updateRole(String search) {
		if(search==null) {
			listRoles = roleManuelService.findAllOrderByDateMaj();
		} else {
			listRoles = roleManuelService.findFor(search);
		}
		dataProvider = new ListDataProvider<>(listRoles);
		rolesGrid.setDataProvider(dataProvider);
	}


	private void notifyClicked() {
		Notification.show(getTranslation("rolemanuel.clicked", LocalTime.now()));
		updateRole(champRecherche.getValue());
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("rolemanuel.title"));

		button.setText(getTranslation("rolemanuel.refresh"));
		buttonCsv.setText(getTranslation("rolemanuel.csv"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
