package fr.univlorraine.publikfeed.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.RouterLink;

@SuppressWarnings("serial")
public class DrawerRouterLink extends RouterLink implements LocaleChangeObserver {

	private final Span text = new Span();

	private final String textKey;

	public DrawerRouterLink(final VaadinIcon icon, final String textKey, final Class<? extends Component> navigationTarget) {
		super(null, navigationTarget);
		this.textKey = textKey;

		HorizontalLayout linkLayout = new HorizontalLayout(new Icon(icon), text);
		linkLayout.setAlignItems(Alignment.CENTER);
		linkLayout.setWidthFull();
		linkLayout.getStyle().set("margin", "var(--lumo-space-s) var(--lumo-space-s) var(--lumo-space-s) 0.75rem");
		add(linkLayout);
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		text.setText(getTranslation(textKey));
	}

}
