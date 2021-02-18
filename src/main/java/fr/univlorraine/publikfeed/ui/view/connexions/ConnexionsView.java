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
