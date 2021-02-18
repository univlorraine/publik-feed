package fr.univlorraine.publikfeed.ui.view.apropos;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.service.SecurityService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class AProposView extends Div implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	@Autowired
	private transient SecurityService securityService;
	@Autowired
	private transient BuildProperties buildProperties;

	private final H4 userDivTitle = new H4();
	private final TextField username = new TextField();
	private final TextField roles = new TextField();

	@PostConstruct
	private void init() {
		getStyle().set("padding", "1em");

		initAppInfo();
		initUserInfo();
	}

	private void initAppInfo() {
		H2 nameComp = new H2(buildProperties.getName());
		nameComp.getStyle().set("display", "inline");
		add(nameComp);

		add(new Span(" v" + buildProperties.getVersion()));

		Element buildTimeElement = new Element("small");
		ZonedDateTime buildTime = buildProperties.getTime().atZone(ZoneId.systemDefault());
		buildTimeElement.setText(' ' + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(buildTime));
		buildTimeElement.getStyle().set("color", "var(--lumo-secondary-text-color)");
		getElement().appendChild(buildTimeElement);

		Paragraph descriptionComp = new Paragraph(buildProperties.get("description"));
		descriptionComp.getStyle().set("font-style", "italic");
		add(descriptionComp);
	}

	private void initUserInfo() {
		userDivTitle.getStyle().set("margin-bottom", "0");
		add(userDivTitle);

		securityService.getUsername().ifPresent(username::setValue);
		username.setReadOnly(true);

		securityService.getPrincipal()
			.map(UserDetails::getAuthorities)
			.map(authorities -> authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")))
			.ifPresent(roles::setValue);
		roles.setReadOnly(true);

		FormLayout userForm = new FormLayout(username, roles);
		add(userForm);
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("apropos.title"));

		userDivTitle.setText(getTranslation("apropos.usertitle"));
		username.setLabel(getTranslation("apropos.field.username"));
		roles.setLabel(getTranslation("apropos.field.roles"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}