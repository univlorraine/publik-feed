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
