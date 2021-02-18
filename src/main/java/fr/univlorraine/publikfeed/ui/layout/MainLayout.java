package fr.univlorraine.publikfeed.ui.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.shared.ui.Transport;

import fr.univlorraine.publikfeed.service.CurrentUiService;
import fr.univlorraine.publikfeed.service.SecurityService;
import fr.univlorraine.publikfeed.ui.component.AppColorStyle;
import fr.univlorraine.publikfeed.ui.view.apropos.AProposView;
import fr.univlorraine.publikfeed.ui.view.connexions.ConnexionsView;
import fr.univlorraine.publikfeed.ui.view.main.MainView;
import fr.univlorraine.publikfeed.config.SecurityConfig;
import fr.univlorraine.publikfeed.model.app.entity.Utilisateur;
import fr.univlorraine.publikfeed.utils.ReactiveUtils;

@Push(transport = Transport.WEBSOCKET_XHR)
@JsModule("./src/set-dark-mode.js")
@JsModule("./src/font-open-sans.js")
@CssImport(value = "./styles/lumo-font-family.css")
@CssImport(value = "./styles/vaadin-app-layout.css", themeFor = "vaadin-app-layout")
@CssImport(value = "./styles/vaadin-button-pointer.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/vaadin-button-pointer.css", themeFor = "vaadin-drawer-toggle")
@CssImport(value = "./styles/vaadin-button-pointer.css", themeFor = "vaadin-menu-bar-button")
@CssImport(value = "./styles/vaadin-menu-bar-user.css", themeFor = "vaadin-menu-bar")
@CssImport(value = "./styles/vaadin-context-menu-list-box-pointer.css", themeFor = "vaadin-context-menu-list-box")
@CssImport(value = "./styles/vaadin-checkbox-pointer.css", themeFor = "vaadin-checkbox")
@CssImport(value = "./styles/vaadin-grid.css", themeFor = "vaadin-grid")
@SuppressWarnings("serial")
public class MainLayout extends AppLayout implements PageConfigurator, BeforeEnterObserver, LocaleChangeObserver {

	@Autowired
	private transient CurrentUiService currentUiService;
	@Autowired
	private transient AppTitle appTitle;
	@Autowired
	private transient SecurityService securityService;

	@Value("${doc.url:}")
	private transient String docUrl;
	@Value("${help.url:}")
	private transient String helpUrl;

	private final Tabs tabs = new Tabs();
	private final Map<Class<? extends Component>, Tab> navigationTargetToTab = new HashMap<>();

	private final Div navBarHeader = new Div();
	private MenuItem userMenuAproposItem;
	private MenuItem userMenuLogoutItem;

	@PostConstruct
	public void init() {
		/* Theme: Mode sombre */
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getDarkModeFlux()
				.map(darkMode -> () -> getElement().executeJs("setDarkMode($0)", darkMode)));

		/* Theme: Couleur principale */
		AppColorStyle appColorStyle = new AppColorStyle();
		ReactiveUtils.subscribeWhenAttached(this,
			currentUiService.getAppColorFlux()
				.map(appColor -> () -> appColorStyle.setColor(appColor)));
		getElement().appendChild(appColorStyle.getElement());

		/* Menu au-dessus de la barre d'application */
		setPrimarySection(Section.DRAWER);

		/* Titre du menu */
		addToDrawer(appTitle);

		/* Menu */
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addSelectedChangeListener(event -> {
			/* Seules les actions de navigation doivent pouvoir changer la tab sélectionnée. */
			if (event.isFromClient()) {
				tabs.setSelectedTab(event.getPreviousTab());
			}
		});
		addDrawerRouterLink(VaadinIcon.HOME, "home.title", MainView.class);
		addDrawerRouterLink(VaadinIcon.BAR_CHART_H, "connexions.title", ConnexionsView.class);
		if (!docUrl.isBlank()) {
			addDrawerHrefLink(VaadinIcon.BOOK, "menu.doc", docUrl, true);
		}
		if (!helpUrl.isBlank()) {
			addDrawerHrefLink(VaadinIcon.LIFEBUOY, "menu.help", helpUrl, true);
		}
		addToDrawer(tabs);

		/* Bouton de basculement du menu dans la barre d'application */
		addToNavbar(new DrawerToggle());

		navBarHeader.getStyle()
			.set("flex", "1")
			.set("margin", "0 var(--lumo-space-s) 0 0");
		addToNavbar(navBarHeader);

		if (securityService.isUserLoggedIn()) {
			securityService.getPrincipal()
				.map(this::createUserMenu)
				.ifPresent(this::addToNavbar);
		}
	}

	private MenuBar createUserMenu(final Utilisateur utilisateur) {
		MenuBar topMenu = new MenuBar();
		topMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
		topMenu.addClassName("user-menu");

		MenuItem userItem = topMenu.addItem(createUserImage(utilisateur));
		SubMenu userMenu = userItem.getSubMenu();

		String name = Optional.ofNullable(utilisateur.getDisplayName())
			.or(() -> Optional.ofNullable(utilisateur.getUsername()))
			.orElse("-");
		MenuItem usernameItem = userMenu.addItem(name);
		usernameItem.setEnabled(false);
		usernameItem.getElement()
			.getStyle()
			.set("color", "var(--lumo-primary-color)")
			.set("text-align", "center");

		userMenu.add(new Hr());

		userMenuAproposItem = userMenu.addItem((String) null, event -> getUI().ifPresent(ui -> ui.navigate(AProposView.class)));

		userMenuLogoutItem =
			userMenu.addItem((String) null,
				event -> getUI().map(UI::getPage).ifPresent(page -> page.executeJs("window.open('" + SecurityConfig.LOGOUT_URL + "', '_self')")));

		return topMenu;
	}

	private Component createUserImage(final Utilisateur utilisateur) {
		String displayName = utilisateur.getDisplayName();
		if (displayName == null || displayName.isBlank()) {
			Icon icon = new Icon(VaadinIcon.USER);
			icon.addClassName("user-image");
			icon.getStyle().set("padding-top", "5px");
			return icon;
		} else {
			Div div = new Div();
			div.addClassName("user-image");

			String initials = displayName.replaceAll("\\W|(?<=\\w)\\w", "");
			if (initials.length() > 4) {
				initials = initials.substring(0, 4);
			}
			div.setText(initials);

			if (initials.length() == 3) {
				div.getStyle().set("font-size", "small");
			} else if (initials.length() == 4) {
				div.getStyle().set("font-size", "x-small");
			}
			return div;
		}
	}

	private void addDrawerRouterLink(final VaadinIcon icon, final String textKey, final Class<? extends Component> navigationTarget) {
		if (securityService.isAccessGranted(navigationTarget)) {
			DrawerRouterLink routerLink = new DrawerRouterLink(icon, textKey, navigationTarget);
			Tab tab = new Tab(routerLink);
			tabs.add(tab);
			navigationTargetToTab.put(navigationTarget, tab);
		}
	}

	private void addDrawerHrefLink(final VaadinIcon icon, final String textKey, final String href, final boolean openInNewTab) {
		DrawerHrefLink link = new DrawerHrefLink(icon, textKey, href, openInNewTab);
		Tab tab = new Tab(link);
		tabs.add(tab);
	}

	/**
	 * Sélectionne l'onglet adéquat.
	 * @see com.vaadin.flow.router.internal.BeforeEnterHandler#beforeEnter(com.vaadin.flow.router.BeforeEnterEvent)
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		tabs.setSelectedTab(navigationTargetToTab.get(event.getNavigationTarget()));
	}

	/**
	 * @see com.vaadin.flow.server.PageConfigurator#configurePage(com.vaadin.flow.server.InitialPageSettings)
	 */
	@Override
	public void configurePage(final InitialPageSettings settings) {
		settings.addFavIcon("icon", "/favicon-32x32.png", "32x32");
		settings.addFavIcon("icon", "/favicon-16x16.png", "16x16");
	}

	/**
	 * @see com.vaadin.flow.i18n.LocaleChangeObserver#localeChange(com.vaadin.flow.i18n.LocaleChangeEvent)
	 */
	@Override
	public void localeChange(final LocaleChangeEvent event) {
		if (userMenuAproposItem != null) {
			userMenuAproposItem.setText(getTranslation("apropos.title"));
		}
		if (userMenuLogoutItem != null) {
			userMenuLogoutItem.setText(getTranslation("menu.exit"));
		}

		/* Initialise les messages indiquant la perte de connexion. */
		getUI().map(UI::getReconnectDialogConfiguration)
			.ifPresent(reconnectDialogConfiguration -> {
				reconnectDialogConfiguration.setDialogText(getTranslation("vaadin.reconnectDialog.text"));
				reconnectDialogConfiguration.setDialogTextGaveUp(getTranslation("vaadin.reconnectDialog.textGaveUp"));
			});
	}

	/**
	 * @see com.vaadin.flow.component.applayout.AppLayout#showRouterLayoutContent(com.vaadin.flow.component.HasElement)
	 */
	@Override
	public void showRouterLayoutContent(final HasElement content) {
		super.showRouterLayoutContent(content);
		if (content instanceof HasHeader) {
			navBarHeader.add(((HasHeader) content).getHeader());
		}
	}

	/**
	 * @see com.vaadin.flow.router.RouterLayout#removeRouterLayoutContent(com.vaadin.flow.component.HasElement)
	 */
	@Override
	public void removeRouterLayoutContent(final HasElement oldContent) {
		super.removeRouterLayoutContent(oldContent);
		if (oldContent instanceof HasHeader) {
			((HasHeader) oldContent).getHeader().getElement().removeFromParent();
		}
	}

}
