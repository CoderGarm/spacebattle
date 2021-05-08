package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.SBPageActionSelectorLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemOverviewDisplay;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringComponent
@UIScope
@Route(value = StarMapMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = StarMapMainView.ROUTE, layout = MainView.class)
public class StarMapMainView extends SBPageActionSelectorLayout<StarSystemLayout> {

    @Nonnull
    public static final String ROUTE = "starmap";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private User user;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService starsystemService;

    @Nonnull
    private StarSystemLayout content;

    @Nonnull
    private final StarSystemOverviewDisplay starSystemOverviewDisplay;

    @Nonnull
    private final StarSystemDisplay starSystemDisplay;

    @Nonnull
    private Set<StarSystem> starSystems = new HashSet<>();

    @Autowired
    public StarMapMainView(@Nonnull final UserService userService,
                           @Nonnull final PlanetService planetService,
                           @Nonnull final StarSystemService starsystemService,
                           @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(starsystemService, "starsystemService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.planetService = planetService;
        this.starsystemService = starsystemService;
        User loggedIn = userService.getLoggedInUser();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;
        starSystems.addAll(starsystemService.findAll());
        starSystemDisplay = new StarSystemDisplay();
        starSystemOverviewDisplay = new StarSystemOverviewDisplay();
        starSystemOverviewDisplay.setValue(starSystems);
        createActionSelectorMenu();

        content = setContent(starSystemOverviewDisplay);
        final Map<Tab, Boolean> readOnlyMap = new HashMap<>();
        final Tab tab = getTabForComponentOfActionMenu(starSystemDisplay);
        readOnlyMap.put(tab, false);
        updateActionMenuUsability(readOnlyMap);

        defineRefresh();
    }

    /**
     * This refreshing is necessary because a {@link Svg} will clear it's own content if out of scope.
     */
    private void defineRefresh() {
        UI.getCurrent().addBeforeEnterListener(event -> {
            if (event.getNavigationTarget().getName().equals(this.getClass().getName())) {
                starSystemOverviewDisplay.refresh();
                starSystemDisplay.refresh();
            }
        });
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.DISPLAY_PLANETARY_SYSTEM.name())) {
            final StarSystem starSystem = (StarSystem) e.getSource();
            starSystemDisplay.setValue(starSystem);
            content = setContent(starSystemDisplay);
            final Map<Tab, Boolean> readOnlyMap = new HashMap<>();
            final Tab tab = getTabForComponentOfActionMenu(starSystemDisplay);
            readOnlyMap.put(tab, true);
            updateActionMenuUsability(readOnlyMap);
            selectTabOfActionMenu(content);
        }
    }

    @Override
    protected void createActionSelectorMenu() {
        final Tab overview = new Tab("Universe map");
        addComponentForTabOfActionMenu(overview, starSystemOverviewDisplay);

        final Tab system = new Tab("Star system map");
        addComponentForTabOfActionMenu(system, starSystemDisplay);
        addActionListener();
    }

    @Override
    protected void updateActionMenuUsability(@Nullable Map<Tab, Boolean> readOnlyMap) {
        if (readOnlyMap == null) {
            return;
        }
        actionSelectorMenu.getChildren().forEach(menuItem -> {
            final Tab tab = (Tab) menuItem;
            final Boolean aBoolean = readOnlyMap.get(tab);
            if (aBoolean != null) {
                tab.setEnabled(aBoolean);
            }
        });
    }

    @Override
    protected void addActionListener() {
        actionSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            final StarSystemLayout componentForTab = getComponentForTabOfActionMenu(selectedTab);
            content = setContent(componentForTab);
            content.refresh();
        });
    }
}
