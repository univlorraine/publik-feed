package fr.univlorraine.publikfeed.ui.layout;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.UIScope;

import fr.univlorraine.publikfeed.service.CurrentUiService;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;

@Component
@UIScope
@SuppressWarnings("serial")
public class AppTitle extends HorizontalLayout implements LocaleChangeObserver {

	private static final String SRC_LOGO = "./images/logo.png";
	private static final String SRC_LOGO_NB = "./images/logo-nb.png";

	@Autowired
	private transient CurrentUiService currentUiService;
	@Autowired
	private transient BuildProperties buildProperties;

	private final Image logo = new Image();

	@PostConstruct
	private void init() {
		setAlignItems(Alignment.END);
		getStyle().set("margin", "0.75rem 0.75rem 0.75rem 1.5rem");

		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getDarkModeFlux()
				.map(darkMode -> darkMode ? SRC_LOGO_NB : SRC_LOGO)
				.map(logoSrc -> () -> logo.setSrc(logoSrc)));
		add(logo);

		Div appNameTitle = new Div(new Text(buildProperties.getName()));
		appNameTitle.getElement().getStyle().set("font-size", "var(--lumo-font-size-xl)");
		add(appNameTitle);
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		logo.setAlt(getTranslation("menu.alt-logo"));
	}

}
