package fr.univlorraine.publikfeed.ui.view.demo;

import java.time.LocalTime;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import fr.univlorraine.publikfeed.service.CurrentUiService;
import fr.univlorraine.publikfeed.ui.layout.HasHeader;
import fr.univlorraine.publikfeed.ui.layout.MainLayout;
import fr.univlorraine.publikfeed.ui.layout.PageTitleFormatter;
import fr.univlorraine.publikfeed.ui.layout.TextHeader;
import fr.univlorraine.publikfeed.utils.CSSColorUtils;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;
import lombok.Getter;

@Route(layout = MainLayout.class)
@SuppressWarnings("serial")
public class DemoView extends VerticalLayout implements HasDynamicTitle, HasHeader, LocaleChangeObserver {

	@Autowired
	private transient CurrentUiService currentUiService;
	@Autowired
	private transient PageTitleFormatter pageTitleFormatter;
	@Getter
	private String pageTitle = "";
	@Getter
	private final TextHeader header = new TextHeader();

	private final Checkbox darkModeCB = new Checkbox();

	private final TextField colorTF = new TextField();

	private final Button button = new Button();

	@PostConstruct
	public void init() {
		initSetBaseColor();
		initSetDarkMode();
		initTestNotif();
	}

	private void initSetBaseColor() {
		/* Pour changer la couleur de base du theme, injecter le bean
		 * currentUIService et utiliser sa méthode setAppColor([couleur CSS]).
		 *
		 * Pour changer la couleur d'une vue seulement, y ajouter un composant
		 * AppColorStyle, ex: add(new AppColorStyle("rgb(211, 47, 47)")); */
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getAppColorFlux().map(appColor -> () -> colorTF.setValue(appColor)));
		colorTF.addValueChangeListener(event -> {
			String color = event.getValue();
			if (CSSColorUtils.isSupportedColor(color)) {
				colorTF.setInvalid(false);
				currentUiService.setAppColor(color);
			} else {
				colorTF.setInvalid(true);
				colorTF.setErrorMessage(getTranslation("demo.error.invalid-color"));
			}
		});
		colorTF.setValueChangeMode(ValueChangeMode.EAGER);
		add(colorTF);
	}

	private void initSetDarkMode() {
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getDarkModeFlux().map(darkMode -> () -> darkModeCB.setValue(darkMode)));
		darkModeCB.addValueChangeListener(event -> {
			currentUiService.setDarkMode(event.getValue());
		});
		add(darkModeCB);
	}

	private void initTestNotif() {
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> notifyClicked());
		add(button);
	}

	private void notifyClicked() {
		Notification.show(getTranslation("demo.clicked", LocalTime.now()));
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		setViewTitle(getTranslation("demo.title"));

		colorTF.setLabel(getTranslation("demo.color-label"));
		darkModeCB.setLabel(getTranslation("demo.dark-mode-label"));
		button.setText(getTranslation("demo.button"));
	}

	private void setViewTitle(final String viewTitle) {
		pageTitle = pageTitleFormatter.format(viewTitle);
		getUI().map(UI::getPage).ifPresent(page -> page.setTitle(pageTitle));

		header.setText(viewTitle);
	}

}
