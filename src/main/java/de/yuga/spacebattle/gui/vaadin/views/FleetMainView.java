package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.FleetDashboardDisplay;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.FleetLayout;
import de.yuga.spacebattle.gui.vaadin.misc.PageWithSubjectActionTabsAndStats;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@SpringComponent
@UIScope
@Route(value = FleetMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = FleetMainView.ROUTE, layout = MainView.class)
public class FleetMainView extends PageWithSubjectActionTabsAndStats<Fleet> {

    @Nonnull
    public static final String ROUTE = "fleets";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final FleetService fleetService;

    @Nullable
    private Fleet fleet;

    @Nonnull
    private FleetLayout<Fleet> content;

    @Nonnull
    private final FleetDashboardDisplay fleetDashboardDisplay;

    @Autowired
    public FleetMainView(@Nonnull final UserService userService,
                         @Nonnull final FleetService fleetService,
                         @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.fleetService = fleetService;
        final User loggedIn = userService.getLoggedInUser();
        fleetDashboardDisplay = new FleetDashboardDisplay();
        final List<Fleet> allFleetsForUser = fleetService.findAllFleetsBy(loggedIn);
        if (!allFleetsForUser.isEmpty()) {
            fleet = allFleetsForUser.get(0);
        }
        if (fleet != null) {
            fleetDashboardDisplay.updateStatistics(fleet);
        }
        createSubjectSelectorMenu();
        createActionSelectorMenu();
        content = fleetDashboardDisplay;
        setContent(content);
        updateActionMenuUsability(null);
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
        // Stats
        Tab dashboard = new Tab("Dashboard");
        addComponentForTabOfActionMenu(dashboard, fleetDashboardDisplay);

        addActionListener();
    }

    @Override
    protected void addActionListener() {
        actionSelectorMenu.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            StatsLayout<Fleet> componentForTab = getComponentForTabOfActionMenu(selectedTab);
            if (fleet != null) {
                fleet = fleetService.find(fleet);
            }
            componentForTab.updateStatistics(fleet);
            content = setContent((FleetLayout<Fleet>) componentForTab);
        });
    }

    @Override
    protected void createSubjectSelectorMenu() {
        final User loggedInUser = userService.getLoggedInUser();
        List<Fleet> fleetsBy = fleetService.findAllFleetsBy(loggedInUser);
        fleetsBy.forEach(fleet -> {
            final Tab fleetTab = new Tab(fleet.getName());
            addSubjectForTabOfSubjectMenu(fleetTab, fleet);
        });
        addSubjectListener();
    }

    @Override
    protected void addSubjectListener() {
        subjectSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            fleet = getSubjectForTabOfSubjectMenu(selectedTab);
            if (fleet != null) {
                fleet = fleetService.find(fleet);
            }
            addSubjectForTabOfSubjectMenu(selectedTab, fleet);
            content.updateStatistics(fleet);
            getTabForComponentOfActionMenu(content).setSelected(true);
            updateActionMenuUsability(null);
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap) {
        actionSelectorMenu.getChildren().forEach(menuItem -> ((Tab) menuItem).setEnabled(fleet != null));
    }

    @Override
    protected void updateSubjectMenu() {
        final User loggedInUser = userService.getLoggedInUser();
        List<Fleet> fleetsBy = fleetService.findAllFleetsBy(loggedInUser);
        subjectSelectorMenu.getChildren().forEach(component -> {
            Tab tab = (Tab) component;
            Fleet subject = getSubjectForTabOfSubjectMenu(tab);
            if (!fleetsBy.contains(subject)) {
                removeFromSubject(tab);
            }
        });

        fleetsBy.stream().filter(fleet -> !subjectSelectorObject.containsValue(fleet)).forEach(fleet -> {
            Tab subjectTab = new Tab(fleet.getName());
            addSubjectForTabOfSubjectMenu(subjectTab, fleet);
        });

        addSubjectListener();
    }
}
