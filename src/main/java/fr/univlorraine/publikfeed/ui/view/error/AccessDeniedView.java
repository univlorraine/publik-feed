package fr.univlorraine.publikfeed.ui.view.error;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class AccessDeniedView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Label label = new Label();

	@Value("${help.url:}")
	private transient String helpUrl;
	private final Label helpLabel = new Label();
	private final Button helpButton = new Button(new Icon(VaadinIcon.LIFEBUOY));

	@PostConstruct
	public void init() {
		add(label);

		if (!helpUrl.isBlank()) {
			add(new Div(helpLabel, makeHelpLink()));
		}
	}

	private Anchor makeHelpLink() {
		helpButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Anchor helpLink = new Anchor(helpUrl, helpButton);
		helpLink.setTarget("_blank");
		/* cf. https://stackoverflow.com/a/17711167/2477444 */
		helpLink.getElement().setAttribute("rel", "noopener noreferrer");
		return helpLink;
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("error.accessdenied"));

		label.setText(getTranslation("error.accessdenied.detail"));
		helpLabel.setText(getTranslation("error.accessdenied.help"));
		helpButton.setText(getTranslation("menu.help"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
