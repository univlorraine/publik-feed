package fr.univlorraine.publikfeed.ui.view.rolemanuel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import elemental.json.Json;
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
	VerticalLayout listImportLayout = new VerticalLayout();
	HorizontalLayout importButtonLayout = new HorizontalLayout();
	private final Button buttonNew = new Button();
	private final Button buttonCancel= new Button();
	private final Button buttonCreate= new Button();
	private final TextField champRecherche = new TextField();
	private final Button buttonCsv = new Button();
	private final Button buttonImportList = new Button();
	private final TextField infoFormatCsv = new TextField(getTranslation("rolemanuel.infocsvtitle"));
	ProgressBar progressBar = new ProgressBar();
	Label statusLabel = new Label();

	private int nbRoleImporte = 0;

	private MemoryBuffer memoryBuffer = new MemoryBuffer();
	private Upload upload = new Upload(memoryBuffer);

	TextField idField = new TextField();
	TextField libField = new TextField();
	TextField filtreField = new TextField();
	TextField loginsField = new TextField();
	TextField loginsDefautField = new TextField();

	Map<String, Boolean> mapRoleImportStatus = new HashMap<String, Boolean> ();
	List<RoleManuel> listRoleToImport = new LinkedList<RoleManuel> ();
	Map<String, String> mapAnomalieRoleImport = new HashMap<String, String> ();

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


	private final Grid<RoleManuel> importGrid = new Grid<>();
	private final Column<RoleManuel> etatImportColumn = importGrid.addComponentColumn(r -> getEtatImportColumn(r))
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("Etat");
	private final Column<RoleManuel> idImportColumn = importGrid.addColumn(r -> r.getId())
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("ID");
	private final Column<RoleManuel> libelleImportColumn = importGrid.addColumn(r -> r.getLibelle())
		.setFlexGrow(1)
		.setAutoWidth(true)
		.setResizable(true).setHeader("Libellé");
	private final Column<RoleManuel> filtreImportColumn = importGrid.addColumn(r -> r.getFiltre())
		.setFlexGrow(1)
		.setAutoWidth(true)
		.setResizable(true).setHeader("Filtre");
	private final Column<RoleManuel> loginsImportColumn = importGrid.addColumn(r -> r.getLogins())
		.setFlexGrow(1)
		.setAutoWidth(true)
		.setResizable(true).setHeader("Logins");
	private final Column<RoleManuel> loginsDefautImportColumn = importGrid.addColumn(r -> r.getLoginsDefaut())
		.setFlexGrow(1)
		.setAutoWidth(true)
		.setResizable(true).setHeader("Logins par defaut");


	List<RoleManuel> listRoles;

	ListDataProvider<RoleManuel> dataProvider;

	ListDataProvider<RoleManuel> dataImportProvider;

	@PostConstruct
	public void init() {
		initJobs();
		initImportLayout();
		initAddForm();
		initGrid();
		importGrid.setItemDetailsRenderer(new ComponentRenderer<>(r -> {
			return getDetailImportColumn(r);
		}));
		rolesGrid.setItemDetailsRenderer(new ComponentRenderer<>(r -> {
			return getDetailColumn(r);
		}));
		this.setHeightFull();
	}


	private Component getIdAndButtonColumn(RoleManuel r) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setAlignItems(Alignment.CENTER);
		Button details = new Button(rolesGrid.isDetailsVisible(r) ? VaadinIcon.ANGLE_UP.create() : VaadinIcon.ANGLE_DOWN.create(),
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
		validButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
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

	
	private Component getDetailImportColumn(RoleManuel r) {
		Label infoLabel = new Label(mapAnomalieRoleImport.get(r.getId()));
		infoLabel.getStyle().set("color", "red");
		infoLabel.getStyle().set("font-style", "oblique");
		return infoLabel;
	}
	
	private Component getEtatImportColumn(RoleManuel r) {
		// par défaut on masque la ligne détail
		importGrid.setDetailsVisible(r,false);
		// Si le role a des anomalies
		if(mapAnomalieRoleImport.containsKey(r.getId())) {
			/*Button bugBtn = new Button( VaadinIcon.BUG.create(),
				e -> importGrid.setDetailsVisible(r, !importGrid.isDetailsVisible(r)));
			return bugBtn;*/
			importGrid.setDetailsVisible(r,true);
			return VaadinIcon.BUG.create();
			
		}
		// Si le role a un status d'import
		if(mapRoleImportStatus.containsKey(r.getId())) {
			// Récupération de l'état de l'import
			Boolean etat = mapRoleImportStatus.get(r.getId());
			if(etat!=null && etat) {
				// Import OK
				return VaadinIcon.CHECK.create();
			}
			if(etat!=null && !etat) {
				// Import KO
				return VaadinIcon.BAN.create();
			}
		}
		return VaadinIcon.CLOCK.create();
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
		TextField loginsDefautField = new TextField("Logins par défaut");
		loginsDefautField.setWidthFull();
		loginsDefautField.setValue(r.getLoginsDefaut() != null ? r.getLoginsDefaut() : "");
		loginsDefautField.setReadOnly(true);
		filtreEtLoginlayout.add(loginsDefautField);
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
		validButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
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
			loginsDefautField.setReadOnly(false);
			editButton.setVisible(false);
			validButton.setVisible(true);
			checkBox.setVisible(true);
		});

		// Modification du role avec les données saisies
		validButton.addClickListener(e -> {
			try {
				RoleManuel updatedRole = roleManuelService.updateFiltreAndLogins(r, filtreField.getValue(), loginsField.getValue(), loginsDefautField.getValue(), checkBox.getValue());
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

	/**
	 * Init du formulaire de création d'un role
	 */
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

		loginsDefautField.setLabel("Logins par défaut");
		loginsDefautField.setPlaceholder("liste de logins par défaut séparés par une virgule");

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
		loginsDefautField.addValueChangeListener(e-> {
			majFormData();
		});

		addLayout.add(idField, libField, filtreField, loginsField, loginsDefautField);
		addLayout.setResponsiveSteps(
			new ResponsiveStep("40em", 1),
			new ResponsiveStep("20em", 2),
			new ResponsiveStep("20em", 3));
		addLayout.setColspan(libField, 2);
		addLayout.setVisible(false);

		add(addLayout);
	}

	/**
	 * Check que les données requises sont renseignées et active ou non le bouton CREATE
	 */
	private void majFormData() {
		// Check les données
		if(formHasAllRequiredValues()) {
			// Affichage du bouton
			buttonCreate.setEnabled(true);
		} else {
			// Non affichage du bouton
			buttonCreate.setEnabled(false);
		}

	}

	/**
	 * Check que les données requises sont renseignées
	 * @return vrai si les données sont ok
	 */
	private boolean formHasAllRequiredValues() {
		return (!StringUtils.isBlank(idField.getValue())
			&& !StringUtils.isBlank(libField.getValue()) 
			&& (!StringUtils.isBlank(filtreField.getValue()) 
				|| !StringUtils.isBlank(loginsField.getValue())));
	}
	
	/**
	 * Check que les données requises sont renseignées
	 * @return vrai si les données sont ok
	 */
	private boolean formHasAllRequiredValues(RoleManuel r) {
		return (!StringUtils.isBlank(r.getId())
			&& !StringUtils.isBlank(r.getLibelle()) 
			&& (!StringUtils.isBlank(r.getFiltre()) 
				|| !StringUtils.isBlank(r.getLogins())));
	}


	/**
	 * Init de la grid des roles présents en base de données
	 */
	private void initGrid() {
		// Init des parametres de la grid
		rolesGrid.setHeightFull();
		rolesGrid.setSelectionMode(SelectionMode.NONE);
		rolesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		rolesGrid.setPageSize(40);
		// Refresh pour afficher tous les roles
		updateRole(null);
		// ajout de la grid au layout
		add(rolesGrid);
	}

	/**
	 * Création du layout d'init (bouton import, progressBar, label de progression et Grid)
	 */
	private void initImportLayout() {
		//Ajout bouton d'import au layout
		buttonImportList.setText("Importer");
		buttonImportList.setIcon(VaadinIcon.CLOUD_DOWNLOAD_O.create());
		importButtonLayout.add(buttonImportList);
		//Ajout progressBar au layout
		progressBar.setWidth("200px");
		progressBar.setHeight("20px");
		progressBar.setVisible(false);
		importButtonLayout.add(progressBar);
		//Ajout statusLabel au layout
		importButtonLayout.add(statusLabel);
		// Clic du bouton pour lancer l'import dans la base
		buttonImportList.addClickListener(e -> {
			log.info("Import des role : {} ",buttonImportList);
			// init des variables de suivi de l'import
			nbRoleImporte = 0;
			buttonImportList.setEnabled(false);
			progressBar.setVisible(true);
			double ratio = nbRoleImporte * 1.0 / listRoleToImport.size();
			progressBar.setValue(ratio);
			statusLabel.setText(nbRoleImporte + " / " + listRoleToImport.size());
			//Lancement de l'import des roles en base
			importList();
		});
		// AJout des composants au layout d'import
		listImportLayout.add(importButtonLayout);
		listImportLayout.add(importGrid);
		listImportLayout.setVisible(false);
		add(listImportLayout);
	}

	/**
	 * Import des roles injectés via CSV dans la base
	 */
	private void importList() {
		if(listRoleToImport!=null && !listRoleToImport.isEmpty()) {
			//Pour chaque role
			for(RoleManuel r : listRoleToImport) {
				try {
					//creer le role dans la base
					r.setDatMaj(LocalDateTime.now());
					r = roleManuelService.saveRole(r);
					//Ajout du status d'import du role dans la map
					mapRoleImportStatus.put(r.getId(), true);
				}catch (Exception e) {
					log.warn("Creation roleManuel dans la base échoué",e);
					//Ajout du status d'import du role dans la map
					mapRoleImportStatus.put(r.getId(), false);
					mapAnomalieRoleImport.put(r.getId(), getTranslation("rolemanuel.importko"));
				}
				//increment du compteur
				nbRoleImporte++;
				//maj de la progressBar
				double ratio = nbRoleImporte * 1.0 / listRoleToImport.size();
				progressBar.setValue(ratio);
				//maj du label
				statusLabel.setText(nbRoleImporte + " / " + listRoleToImport.size());
				//Refresh de la ligne de la grid correspondant au role importé
				importGrid.getDataProvider().refreshItem(r);
			}
		}
	}

	/**
	 * Refresh l'affichage de la grid contenant les roles à importer
	 */
	private void refreshImportLayout() {
		// Maj du contenu de importGrid
		dataImportProvider = new ListDataProvider<>(listRoleToImport);
		importGrid.setDataProvider(dataImportProvider);
		// On affiche le layout contenant la grid
		listImportLayout.setVisible(true);
		// On masque la grid affichant le contenu de la table des roles manuels
		rolesGrid.setVisible(false);
		// Si tous les roles valident les prérequis
		if(mapAnomalieRoleImport.isEmpty()) {
			//Activation du bouton d'import
			buttonImportList.setEnabled(true);
		} else {
			// Désactivation du bouton d'import
			buttonImportList.setEnabled(false);
		}
	}

	/**
	 * Init des boutons d'action
	 */
	private void initJobs() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		// Bouton refresh
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> notifyClicked());
		buttonsLayout.add(button);
		// Champ de recherche
		champRecherche.setAutofocus(true);
		champRecherche.setWidth("300px");
		champRecherche.setClearButtonVisible(true);
		champRecherche.addValueChangeListener( e -> {
			updateRole(champRecherche.getValue());
		});
		buttonsLayout.add(champRecherche);
		// Bouton d'affichage du formulaire de création d'un role
		buttonNew.setText("Nouveau rôle");
		buttonNew.setIcon(VaadinIcon.PLUS.create());
		buttonNew.addClickListener(event -> {
			addLayout.setVisible(true);
			buttonCancel.setVisible(true);
			buttonCreate.setVisible(true);
			buttonCreate.setEnabled(false);
			buttonNew.setVisible(false);
			buttonCsv.setVisible(false);
		});
		buttonsLayout.add(buttonNew);
		// Bouton d'annulation de création d'un role par formulaire
		buttonCancel.setText("Annuler");
		buttonCancel.setIcon(VaadinIcon.ARROW_BACKWARD.create());
		buttonCancel.addClickListener(event -> {
			addLayout.setVisible(false);
			buttonCancel.setVisible(false);
			buttonCreate.setVisible(false);
			buttonNew.setVisible(true);
			buttonCsv.setVisible(true);
		});
		buttonCancel.setVisible(false);
		buttonsLayout.add(buttonCancel);
		// Bouton qui lance la création du role saisi dans le formulaire
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
		// Bouton pour afficher/masquer les outils d'import par CSV
		buttonCsv.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonsLayout.add(buttonCsv);
		buttonsLayout.add(buttonCsv);
		// Réception des données d'un CSV
		upload.addFinishedListener(e -> {
			listRoleToImport.clear();
			mapRoleImportStatus.clear();
			progressBar.setValue(0);
			statusLabel.setText("");
			log.info("Import CSV...");
			InputStream inputStream = memoryBuffer.getInputStream();
			List<String> text = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.toList());
			log.info("-{}",text);
			convertListStringToRoleManuel(text);

		});
		upload.setVisible(false);
		buttonsLayout.add(upload);
		// Gestion de l'affichage lors du clic sur le boutonCsv
		buttonCsv.setText(getTranslation("rolemanuel.csv"));
		buttonCsv.setIcon(VaadinIcon.PLUS.create());
		buttonCsv.addClickListener(e-> {
			upload.setVisible(!upload.isVisible());
			buttonNew.setEnabled(!upload.isVisible());
			if(upload.isVisible()) {
				buttonCsv.setText( "Annuler" );
				buttonCsv.setIcon(VaadinIcon.ARROW_BACKWARD.create());
				infoFormatCsv.setVisible(true);
				rolesGrid.setVisible(false);
			} else {
				listRoleToImport.clear();
				mapRoleImportStatus.clear();
				progressBar.setValue(0);
				statusLabel.setText("");
				infoFormatCsv.setVisible(false);
				upload.getElement().setPropertyJson("files", Json.createArray());
				buttonCsv.setText(getTranslation("rolemanuel.csv") );
				buttonCsv.setIcon(VaadinIcon.PLUS.create());
				rolesGrid.setVisible(true);
				listImportLayout.setVisible(false);
			}
		});

		add(buttonsLayout);
		
		infoFormatCsv.setValue(getTranslation("rolemanuel.infocsv"));
		infoFormatCsv.setReadOnly(true);
		infoFormatCsv.setWidth("25em");
		infoFormatCsv.setVisible(false);
		add(infoFormatCsv);
	}

	/**
	 * Conversion de la liste de String en liste de roleManuel et ajout dans listRoleToImport
	 * @param liste
	 */
	private void convertListStringToRoleManuel(List<String> liste) {
		listRoleToImport.clear();
		mapAnomalieRoleImport.clear();
		if(liste != null ) {
			// Pour chaque String de la liste
			for(String role : liste) {
				// Création d'un RoleManuel correspondant
				RoleManuel r = new RoleManuel();
				String[] attributs = role.split(";");
				r.setId(attributs[0].toUpperCase());
				r.setLibelle(attributs[1]);
				r.setFiltre(attributs[2]);
				r.setLogins(attributs[3]);
				// Si on a une derniere valeur
				if(attributs.length>4) {
					//Il s'agit du login par défaut
					r.setLoginsDefaut(attributs[4]);
				}
				// AJout du role à la liste
				listRoleToImport.add(r);
				
				// Si il manque des infos obligatoires
				if(!formHasAllRequiredValues(r) ) {
					mapAnomalieRoleImport.put(r.getId(),getTranslation("rolemanuel.nonprerequis") );
				}
				// Si le role existe déjà
				if (roleManuelService.findRole(r.getId()).isPresent()) {
					mapAnomalieRoleImport.put(r.getId(),getTranslation("rolemanuel.existedeja") );
				}
			}
			// Refresh de la grid
			refreshImportLayout();
		}
	}


	/**
	 * Création du role manuel renseigné dans le formulaire
	 */
	private RoleManuel creerRoleManuel() {
		boolean ko = false;
		// Vérification des données saisies
		if(StringUtils.isBlank(idField.getValue()) || !idField.getValue().matches("[0-9A-Z][0-9A-Z_]*[0-9A-Z]")) {
			Notification.show("L'id n'est pas valide");
			ko = true;
		}
		if(StringUtils.isBlank(loginsDefautField.getValue())) {
			Notification.show("Vous devez indiquer un login par defaut");
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

		// Création du RoleManuel
		RoleManuel newRole = new RoleManuel();
		newRole.setId(idField.getValue());
		newRole.setLibelle(libField.getValue());
		if(!StringUtils.isBlank(loginsDefautField.getValue())) {
			newRole.setLoginsDefaut(loginsDefautField.getValue());
		}
		if(!StringUtils.isBlank(filtreField.getValue())) {
			newRole.setFiltre(filtreField.getValue());
		}
		if(!StringUtils.isBlank(loginsField.getValue())) {
			newRole.setLogins(loginsField.getValue());
		}
		newRole.setDatMaj(LocalDateTime.now());
		
		// Création du role en base
		newRole = roleManuelService.saveRole(newRole);

		// reset formulaire si création ok
		idField.clear();
		libField.clear();
		filtreField.clear();
		loginsField.clear();
		loginsDefautField.clear();
		
		// On retourne le role
		return newRole;

	}

	/**
	 * Maj de la grid des roles présents en base à partir de la chaine en parametre
	 */
	private void updateRole(String search) {
		// Si la chaine est vide
		if(StringUtils.isBlank(search)) {
			// Récupération de tous les Roles triés
			listRoles = roleManuelService.findAllOrderByDateMaj();
		} else {
			// Recherche des roles en fonction de la chaine en parametre
			listRoles = roleManuelService.findFor(search);
		}
		// Maj du contenu de la grid
		dataProvider = new ListDataProvider<>(listRoles);
		rolesGrid.setDataProvider(dataProvider);
	}

	/**
	 * Nofitication du clic et lancement de refresh de la grid
	 */
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
		//buttonCsv.setText(getTranslation("rolemanuel.csv"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
