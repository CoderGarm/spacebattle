package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.account.UserView;
import de.yuga.spacebattle.gui.vaadin.account.info.InfoView;
import de.yuga.spacebattle.gui.vaadin.account.info.LoginView;
import de.yuga.spacebattle.gui.vaadin.dashboard.DashboardView;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@SpringComponent
@PreserveOnRefresh
@Controller
@UIScope
@Theme(value = Material.class/*, variant = Material.DARK*/)
@CssImport("./styles/views/main/main-view.css")
@PWA(name = "Spacebattle", shortName = "SB", enableInstallPrompt = false)
@JsModule("./styles/shared-styles.js")
@Route
public class MainView extends AppLayout {

    /**
     * Strange Exception when the UID is another one.
     */
    private static final long serialVersionUID = 4136300596358225703L;

    private final static Logger LOGGER = LoggerFactory.getLogger(MainView.class);

    public static final String MAX_WIDTH = "80%";

    public static final Class<?>[] NO_LOGIN_NEEDED_TARGETS = {
            LoginView.class,
            InfoView.class
    };

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    private Tabs menu;
    private H1 viewTitle;
    private Component headerContent;
    private Component drawerContent;
    private final LoginOverlay loginOverlay = new LoginOverlay();
    private final Button logout;
    private final Button login;


    @Autowired
    public MainView(@Nonnull final UserService userService, @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.userService = userService;
        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);

        createLogin();
        logout = createLogoutButton();
        login = createLoginButton();
        setPrimarySection(Section.DRAWER);
        headerContent = createHeaderContent();
        addToNavbar(true, headerContent);
        menu = createMenu();
        drawerContent = createDrawerContent(menu);
        addToDrawer(drawerContent);
    }

    private void createLogin() {
        loginOverlay.addLoginListener(e -> {
            final User user = this.userService.login(e.getUsername(), e.getPassword());
            if (user != null) {
                this.userService.setLogin(user);
                loginOverlay.close();
                getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
                uiEventBus.publish(this, ESBEvent.LOGIN.name());
            } else {
                loginOverlay.setError(true);
                loginOverlay.setEnabled(true);
            }
        });
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation("To close the login form submit non-empty username and password");
        loginOverlay.setI18n(i18n);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.LOGIN.name())) {
            LOGGER.debug("logged in");
            createMenuByLoginState();
        } else {
            LOGGER.debug("not this event");
        }
    }


    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H1();
        layout.add(viewTitle);
        if (userService.isLoggedIn() != null) {
            layout.add(logout);
        } else {
            layout.add(login);
        }
        layout.add(new Avatar());
        return layout;
    }

    @Nonnull
    private Button createLoginButton() {
        final Button login;
        login = new Button("Login", e -> loginOverlay.setOpened(true));
        login.setClassName("first-on-the-right");
        return login;
    }

    @Nonnull
    private Button createLogoutButton() {
        final Button logout;
        logout = new Button("Logout", e -> {
            this.userService.setLogin(null);
            createMenuByLoginState();
            getUI().ifPresent(ui -> ui.navigate(MainView.class));
        });
        logout.setClassName("first-on-the-right");
        return logout;
    }

    private void createMenuByLoginState() {
        remove(headerContent);
        headerContent = createHeaderContent();
        addToNavbar(true, headerContent);
        remove(drawerContent);
        menu = createMenu();
        drawerContent = createDrawerContent(menu);
        addToDrawer(drawerContent);
    }

    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "Spacebattle logo"));
        logoLayout.add(new H1("Spacebattle"));
        layout.add(logoLayout, menu);
        layout.setHeightFull();
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setId("tabs");

        Tab login = createTab("Login", LoginView.class);
        Tab info = createTab("Info", InfoView.class);
        if (userService.isLoggedIn() != null) {
            tabs.add(createMenuItems());
            Accordion accordion = new Accordion();
            AccordionPanel accordionPanel = new AccordionPanel();
            accordionPanel.setSummaryText("Want to know more?");
            accordionPanel.addContent(login, info);
            accordionPanel.addThemeVariants(DetailsVariant.FILLED);
            accordion.add(accordionPanel);
            accordion.close();
            tabs.add(accordion);
        } else {
            tabs.add(login, info);
        }
        tabs.setHeight("80%");
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{
                createTab("Dashboard", DashboardView.class),
                createTab("User", UserView.class)
        };
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}