package fr.univlorraine.publikfeed.ui.view.main;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.job.services.JobLauncher;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.services.ProcessHisService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.JobUtils;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class MainView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Resource
	private ProcessHisService processHisService;
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

	private final Grid<ProcessHis> processGrid = new Grid<>();
	private final Column<ProcessHis> codeColumn = processGrid.addColumn(ph -> ph.getId().getCodProcess())
		.setFlexGrow(1)
		.setAutoWidth(true)
		.setFrozen(true)
		.setResizable(true).setHeader("Code");;
	private final Column<ProcessHis> stateColumn = processGrid.addComponentColumn(ph -> getStateColumn(ph))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Etat");
	private final Column<ProcessHis> avancementColumn = processGrid.addComponentColumn(ph -> getAvancementColumn(ph))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Progression");
	private final Column<ProcessHis> anomalieColumn = processGrid.addColumn(ph -> ph.getNbObjErreur())
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Nb erreur");
	private final Column<ProcessHis> datDebColumn = processGrid.addColumn(ph -> Utils.formatDateForDisplay(ph.getId().getDatDeb()))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Debut");
	private final Column<ProcessHis> datFinColumn = processGrid.addColumn(ph -> Utils.formatDateForDisplay(ph.getDatFin()))
		.setFlexGrow(1)
		.setAutoWidth(true).setHeader("Date Fin");

	List<ProcessHis> listJobs;

	@PostConstruct
	public void init() {
		initJobs();
		initGrid();
	}

	private Component getAvancementColumn(ProcessHis ph) {
		HorizontalLayout vl = new HorizontalLayout();
		String etat = JobUtils.getStatus(ph.getId().getCodProcess());
		if(etat!=null) {
			Label label = new Label(ph.getNbObjTraite()+"/"+ph.getNbObjTotal());
			label.getStyle().set("margin", "auto");
			vl.add(label);
			if(etat!=null && etat.equals(JobUtils.RUNNING) && ph.getNbObjTotal() != 0) {
				ProgressBar progressBar = new ProgressBar();
				double ratio = ph.getNbObjTraite() * 1.0 / ph.getNbObjTotal();
				progressBar.setValue(ratio);
				progressBar.setWidth("200px");
				progressBar.setHeight("20px");
				vl.add(progressBar);
			}
		}
		return vl;
	}
	
	private Component getStateColumn(ProcessHis ph) {
		HorizontalLayout vl = new HorizontalLayout();
		String etat = JobUtils.getStatus(ph.getId().getCodProcess());
		if(etat!=null) {
			Label label = new Label(etat);
			label.getStyle().set("margin", "auto");
			vl.add(label);
			if(etat!=null && etat.equals(JobUtils.OFF)) {
				Button runBtn = new Button();
				runBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				runBtn.setText(getTranslation("home.run"));
				runBtn.addClickListener(event -> run(ph.getId().getCodProcess()));
				vl.add(runBtn);
			}
		}
		return vl;
	}

	private void run(String jobName) {
		new Thread(() -> {
				jobLauncher.launch(jobName);
			}).start();
		// maj de la vue
		updateProcess();
	}

	private void initGrid() {

		processGrid.setHeightByRows(true);
		processGrid.setSelectionMode(SelectionMode.NONE);
		processGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		listJobs = processHisService.getListJobs();
		processGrid.setDataProvider(DataProvider.ofCollection(listJobs));

		add(processGrid);
	}

	private void initJobs() {
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> notifyClicked());
		add(button);
	}

	private void updateProcess() {
		listJobs = processHisService.getListJobs();
		processGrid.setDataProvider(DataProvider.ofCollection(listJobs));
	}


	private void notifyClicked() {
		Notification.show(getTranslation("home.clicked", LocalTime.now()));
		updateProcess();
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("home.title"));

		button.setText(getTranslation("home.refresh"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
