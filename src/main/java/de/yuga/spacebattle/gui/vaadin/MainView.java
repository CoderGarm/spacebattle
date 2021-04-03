package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.gui.impl.DefaultApiImpl;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.turn.TickDisplay;
import de.yuga.spacebattle.gui.vaadin.views.DashboardView;
import de.yuga.spacebattle.gui.vaadin.views.LoginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.SBRouting.SB_ROUTING_ITEMS;
import static de.yuga.spacebattle.gui.vaadin.ViewHelper.MAX_MENU_HEIGHT;

/**
 * The main view is a top-level placeholder for other views.
 */
@SpringComponent
@PreserveOnRefresh
@Controller
@UIScope
@Theme(value = Material.class, variant = Material.DARK)
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

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final DefaultApiImpl defaultApi;

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private H1 viewTitle = new H1("Spacebattle");

    @Nonnull
    private final LinkedHashMap<Tab, Class<? extends Component>> tabs = createTabs();

    @Nonnull
    private final Tabs menu = createMenu();

    @Nonnull
    private final MenuBar wantToKnowMore = createWantToKnowMore();

    @Nonnull
    private final LoginOverlay loginOverlay = createLogin();

    @Nonnull
    private final Button logoutButton = createLogoutButton();

    @Nonnull
    private final Button initialDataButton = createCreateInitialDataButton();

    @Nonnull
    private final Button loginButton = createLoginButton();

    @Nonnull
    private final TickDisplay tickDisplay = new TickDisplay();


    @Autowired
    public MainView(@Nonnull final UserService userService,
                    @Nonnull final TickService tickService,
                    @Nonnull final DefaultApiImpl defaultApi,
                    @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");
        Preconditions.checkNotNull(defaultApi, "defaultApi shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.userService = userService;
        this.tickService = tickService;
        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.defaultApi = defaultApi;

        createLogin();
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent(menu));
        updateMenu();
    }

    private LoginOverlay createLogin() {
        LoginOverlay loginOverlay = new LoginOverlay();
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
        return loginOverlay;
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.LOGIN.name())) {
            updateMenu();
        }
        userService.refresh();
    }

    private LinkedHashMap<Tab, Class<? extends Component>> createTabs() {
        final LinkedHashMap<Tab, Class<? extends Component>> tabMap = new LinkedHashMap<>();
        for (int i = 0; i < SB_ROUTING_ITEMS.length; i++) {
            SBRouting item = SB_ROUTING_ITEMS[i];
            Class<? extends Component> clazz = item.getClazz();
            Tab tab = createTab(item.getNavText(), clazz);

            tabMap.put(tab, clazz);
        }
        return tabMap;
    }


    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        tickDisplay.updateTick(tickService.getLatest());
        tickDisplay.setMargin(true);
        layout.add(tickDisplay);
        viewTitle = new H1();
        layout.add(viewTitle);
        layout.add(wantToKnowMore);
        layout.add(this.logoutButton);
        layout.add(this.loginButton);
        List<User> all = userService.findAll();
        if (all.isEmpty()) {
            layout.add(initialDataButton);
        }
        layout.add(new Avatar());
        return layout;
    }

    @Nonnull
    private Button createLoginButton() {
        final Button login;
        //login = new Button("Login", e -> loginOverlay.setOpened(true));

        login = new Button("Login", e -> {
            final User user = this.userService.login("flashkid", "test");
            if (user != null) {
                this.userService.setLogin(user);
                loginOverlay.close();
                getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
                uiEventBus.publish(this, ESBEvent.LOGIN.name());
            }
        });

        login.setClassName("first-on-the-right");
        return login;
    }

    @Nonnull
    private Button createLogoutButton() {
        return new Button("Logout", e -> {
            this.userService.setLogin(null);
            updateMenu();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
    }

    @Nonnull
    private Button createCreateInitialDataButton() {
        return new Button("Create Initial Data", e -> {
            ResponseEntity<?> initialData = defaultApi.createInitialData();
            Notification notification = new Notification();
            notification.setDuration(5000);
            switch (initialData.getStatusCode()) {
                case OK:
                    notification.setText("OK");
                    break;
                case BAD_REQUEST:
                    String msg = (String) initialData.getBody();
                    notification.setText(msg);
                    break;
            }
            notification.open();
            updateMenu();
        });
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

    private void updateMenu() {
        boolean isLoggedIn = userService.getLoggedInUser() != null;

        wantToKnowMore.setVisible(isLoggedIn);
        wantToKnowMore.onEnabledStateChanged(isLoggedIn);
        loginButton.setEnabled(!isLoggedIn);
        loginButton.setVisible(!isLoggedIn);
        logoutButton.setEnabled(isLoggedIn);
        logoutButton.setVisible(isLoggedIn);
        menu.getChildren().forEach(view -> {
            Tab tab = (Tab) view;
            this.tabs.keySet().forEach(menuTab -> {
                if (menuTab.equals(tab)) {
                    SBRouting sbRouting = Arrays.stream(SB_ROUTING_ITEMS)
                            .filter(item -> item.getClazz().equals(this.tabs.get(menuTab))).findFirst().orElse(null);
                    if (sbRouting == null) {
                        throw new NotifySBUserException("Someone has stolen a tab while you are afk - call your admin");
                    }
                    boolean needLogin = sbRouting.isLoginNeeded();

                    boolean b = needLogin == isLoggedIn;
                    tab.setVisible(b);
                    tab.setEnabled(b);
                }
            });
        });

        if (userService.findAll().isEmpty()) {
            initialDataButton.setEnabled(true);
            initialDataButton.setVisible(true);
        } else {
            initialDataButton.setEnabled(false);
            initialDataButton.setVisible(false);
        }
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setId("tabs");

        this.tabs.keySet().forEach(tabs::add);
        tabs.setHeight(MAX_MENU_HEIGHT);
        return tabs;
    }


    public static Tab createTab(@Nonnull final String text,
                                @Nonnull final Class<? extends Component> navigationTarget) {
        Preconditions.checkNotNull(text, "text shouldn't be null!");
        Preconditions.checkNotNull(navigationTarget, "navigationTarget shouldn't be null!");

        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    private MenuBar createWantToKnowMore() {
        MenuBar wantToKnowMore = new MenuBar();
        MenuItem menuItem = wantToKnowMore.addItem("You want to know more?");
        wantToKnowMore.setOpenOnHover(false);
        Arrays.stream(SBRouting.SB_ROUTING_ITEMS)
                .filter(view -> !view.isLoginNeeded()).forEach(view -> {
            Class<? extends Component> aClass = view.getClazz();
            menuItem.getSubMenu().addItem(view.getPageName(), e -> getUI().ifPresent(ui -> ui.navigate(aClass)));
        });
        menuItem.getSubMenu().addItem("do Tick", event -> {
            Tick tick = this.tickService.doTick();
            LOGGER.info("do tick");
            tickDisplay.updateTick(tick);
            uiEventBus.publish(this, ESBEvent.TICK_DONE.name());
        });
        wantToKnowMore.setVisible(false);
        wantToKnowMore.onEnabledStateChanged(false);
        wantToKnowMore.setClassName("first-on-the-right");
        return wantToKnowMore;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        Optional<Tab> tabForComponent = getTabForComponent(getContent());
        tabForComponent.ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(@Nullable final Component component) {
        if (component != null) {
            return menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                    .findFirst().map(Tab.class::cast);
        }
        return Optional.empty();
    }

    private String getCurrentPageTitle() {
        String title = Arrays.stream(SB_ROUTING_ITEMS)
                .collect(Collectors.toMap(SBRouting::getClazz, SBRouting::getPageName))
                .get(getContent().getClass());
        return title == null ? "" : title;
    }


}