package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.misc.PageWithActionTabs;
import de.yuga.spacebattle.gui.vaadin.orbitals.colonization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SpringComponent
@UIScope
@Route(value = ColonizationMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ColonizationMainView.ROUTE, layout = MainView.class)
public class ColonizationMainView extends PageWithActionTabs<ColonizationLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColonizationMainView.class);

    @Nonnull
    public static final String ROUTE = "colonize";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private User user;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private ColonizationLayout content;

    @Nonnull
    private final ColonizationDashboardEdit colonizationEdit;

    @Nonnull
    private final ColonizationTaskDashboardDisplay colonizationTaskDisplay;

    @Autowired
    public ColonizationMainView(@Nonnull final UserService userService,
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
        final User loggedIn = userService.getWithKnownStarSystems(userService.getLoggedInUser());
        ;

        colonizationEdit = new ColonizationDashboardEdit();
        colonizationTaskDisplay = new ColonizationTaskDashboardDisplay();
        createActionSelectorMenu();

        content = setContent(colonizationEdit);
        content.setHeightFull();
        refreshAllViews();

        colonizationEdit.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                throw new NotifySBUserException("Nothing to see here, please go on!");
            }
            final StarSystem selectedForBuyingDataStarSystem = event.getValue().getSelectedForBuyingDataStarSystem();
            if (selectedForBuyingDataStarSystem != null) {
                colonizationService.addToKnownSystems(loggedIn, selectedForBuyingDataStarSystem);
            }
            final Planet colonizationSelection = event.getValue().getColonizationSelection();
            if (colonizationSelection != null) {
                colonizationService.startColonizingPlanet(loggedIn, colonizationSelection);
            }
            refreshAllViews();
        });
    }

    /**
     * Refreshes all views.
     */
    private void refreshAllViews() {

        final Set<StarSystem> starSystems = new HashSet<>(starSystemService.findAllUncolonized());
        colonizationEdit.setValue(new ColonizationTransportUniverseDTO(starSystems));

        final User loggedInUser = userService.refresh();
        final List<Colonization> allForUser = colonizationService.findAllForUser(loggedInUser);
        final Set<Colonization> colonizations = new HashSet<>(allForUser);
        colonizationTaskDisplay.setValue(new ColonizationForUserDTO(colonizations));

        final Map<Tab, Boolean> readOnlyMap = new HashMap<>();
        readOnlyMap.put(getTabForComponentOfActionMenu(colonizationTaskDisplay), !colonizations.isEmpty());
        updateActionMenuUsability(readOnlyMap);
    }

    /**
     * Refreshes the given view.
     *
     * @param colonizationLayout the view to refresh
     */
    private void refresh(@Nonnull final ColonizationLayout colonizationLayout) {
        Preconditions.checkNotNull(colonizationLayout, "colonizationLayout shouldn't be null!");

        final Set<Colonization> colonizations = new HashSet<>();
        if (colonizationLayout instanceof ColonizationDashboardEdit) {
            final Set<StarSystem> starSystems = new HashSet<>(starSystemService.findAllUncolonized());
            colonizationEdit.setValue(new ColonizationTransportUniverseDTO(starSystems));
        } else if (colonizationLayout instanceof ColonizationTaskDashboardDisplay) {
            final User loggedInUser = userService.refresh();
            final List<Colonization> allForUser = colonizationService.findAllForUser(loggedInUser);
            colonizations.addAll(allForUser);
            colonizationTaskDisplay.setValue(new ColonizationForUserDTO(colonizations));
        }

        final Map<Tab, Boolean> readOnlyMap = new HashMap<>();
        readOnlyMap.put(getTabForComponentOfActionMenu(colonizationTaskDisplay), !colonizations.isEmpty());
        updateActionMenuUsability(readOnlyMap);
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }

    @Override
    protected void createActionSelectorMenu() {
        addComponentForTabOfActionMenu(new Tab("Organize expansion"), colonizationEdit);
        addComponentForTabOfActionMenu(new Tab("See expansion"), colonizationTaskDisplay);
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
            final ColonizationLayout componentForTab = getComponentForTabOfActionMenu(selectedTab);
            refresh(componentForTab);
            content = setContent(componentForTab);
        });
    }
}
