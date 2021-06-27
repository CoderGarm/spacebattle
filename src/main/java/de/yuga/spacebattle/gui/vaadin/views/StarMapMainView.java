package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.PageWithActionTabs;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.StarSystemOverviewDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class StarMapMainView extends PageWithActionTabs<StarSystemLayout> implements BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(StarMapMainView.class);

    @Nonnull
    public static final String ROUTE = "starmap";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private StarSystemLayout content;

    @Nonnull
    private final StarSystemOverviewDisplay starSystemOverviewDisplay;

    @Nonnull
    private final StarSystemDisplay starSystemDisplay;

    @Autowired
    public StarMapMainView(@Nonnull final UserService userService,
                           @Nonnull final PlanetService planetService,
                           @Nonnull final StarSystemService starSystemService,
                           @Nonnull final ColonizationService colonizationService,
                           @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.planetService = planetService;
        this.starSystemService = starSystemService;
        this.colonizationService = colonizationService;
        final Set<StarSystem> starSystems = new HashSet<>(starSystemService.findAll());
        starSystemDisplay = new StarSystemDisplay();
        starSystemOverviewDisplay = new StarSystemOverviewDisplay();
        starSystemOverviewDisplay.setValue(starSystems);
        createActionSelectorMenu();

        content = setContent(starSystemOverviewDisplay);
        final Map<Tab, Boolean> readOnlyMap = new HashMap<>();
        final Tab tab = getTabForComponentOfActionMenu(starSystemDisplay);
        readOnlyMap.put(tab, false);
        updateActionMenuUsability(readOnlyMap);
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
        } else if (e.getPayload().equals(ESBEvent.TICK_DONE.name())) {
            starSystemOverviewDisplay.refresh();
            starSystemDisplay.refresh();
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
            if (!(content instanceof StarSystemDisplay)) {
                starSystemDisplay.closeDialogs();
            }
            content.refresh();
        });
    }

    /**
     * Detects if a @Route-ed page has left and fires the {@link BeforeLeaveEvent}.
     *
     * @param event the event
     */
    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // remove the "view planed colonization" when leaving the star map
        colonizationService.setColonizationToDisplay(null);
        // close all dialogs at leave
        starSystemDisplay.closeDialogs();
    }

    /**
     * This refreshing is necessary because a {@link Svg} will clear it's own content if out of scope.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        starSystemOverviewDisplay.refresh();
        starSystemDisplay.refresh();
    }
}
