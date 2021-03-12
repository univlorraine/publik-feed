package fr.univlorraine.publikfeed.ui.view.users;

import java.time.LocalTime;
import java.util.List;
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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.job.services.JobLauncher;
import fr.univlorraine.publikfeed.model.app.entity.RoleResp;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.services.UserHisService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class UsersView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Resource
	private UserHisService userHisService;
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

	private final Button refreshButton = new Button();
	private final TextField champRecherche = new TextField();
	
	private final Grid<UserHis> usersGrid = new Grid<>();
	private final Column<UserHis> loginColumn = usersGrid.addComponentColumn(r -> getIdAndButtonColumn(r))
		.setFlexGrow(0)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("Login");
	private final Column<UserHis> datMajColumn = usersGrid.addColumn(r -> r.getDatMaj())
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Maj");
	private final Column<UserHis> datSupColumn = usersGrid.addColumn(r -> r.getDatSup())
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

		return detailLayout;
	}

	private void initGrid() {

		usersGrid.setHeightFull();
		usersGrid.setSelectionMode(SelectionMode.NONE);
		usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		updateUsers();

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
			dataProvider.addFilter(role -> StringUtils.containsIgnoreCase(String.valueOf(role.getLogin()), champRecherche.getValue())
				|| StringUtils.containsIgnoreCase(String.valueOf(role.getData()), champRecherche.getValue()));
		});
		buttonsLayout.add(champRecherche);
		
		add(buttonsLayout);
	}
	
	private Component getIdAndButtonColumn(UserHis u) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setAlignItems(Alignment.CENTER);
		Button details = new Button(usersGrid.isDetailsVisible(u) ? VaadinIcon.MINUS.create() : VaadinIcon.PLUS.create(),
			e -> usersGrid.setDetailsVisible(u, !usersGrid.isDetailsVisible(u)));
		Label label = new Label(u.getLogin());
		hl.add(details);
		hl.addAndExpand(label);
		return hl;
	}
	
	private void uploadCsv() {
		Notification.show(getTranslation("Users.clicked", LocalTime.now()));
	}

	private void updateUsers() {
		listUsers = userHisService.findAll();
		dataProvider = DataProvider.ofCollection(listUsers);
		usersGrid.setDataProvider(dataProvider);
	}


	private void notifyClicked() {
		Notification.show(getTranslation("Users.clicked", LocalTime.now()));
		updateUsers();
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("users.title"));

		refreshButton.setText(getTranslation("users.refresh"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
