package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.ConstructBuildingEdit;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.SBPageTopLevelLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED;
import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.ORBITAL_CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED;

@SpringComponent
@UIScope
@Route(value = PlanetMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = PlanetMainView.ROUTE, layout = MainView.class)
public class PlanetMainView extends SBPageTopLevelLayout<Planet> {

    @Nonnull
    public static final String ROUTE = "planets";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private User user;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final JobService jobService;

    @Nullable
    private Planet planet;

    @Nonnull
    private PlanetLayout<Planet> content;

    @Nonnull
    private final PlanetDashboardDisplay planetDashboardDisplay;

    @Nonnull
    private final PlanetShipyardConstructionEdit planetShipyardConstructionEdit;

    @Nonnull
    private final PlanetBuildingConstructionEdit planetBuildingConstructionEdit;

    @Nonnull
    private final PlanetJobDisplay planetJobDisplay;

    @Autowired
    public PlanetMainView(@Nonnull final UserService userService,
                          @Nonnull final PlanetService planetService,
                          @Nonnull final JobService jobService,
                          @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.planetService = planetService;
        this.jobService = jobService;
        User loggedIn = userService.isLoggedIn();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;
        planetDashboardDisplay = new PlanetDashboardDisplay();
        planetBuildingConstructionEdit = new PlanetBuildingConstructionEdit();
        planetShipyardConstructionEdit = new PlanetShipyardConstructionEdit();
        planetJobDisplay = new PlanetJobDisplay();
        content = planetDashboardDisplay;
        createSubjectSelectorMenu();
        createActionSelectorMenu();
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

        if (planet == null) {
            throw new NotifySBUserException("Something went wrong - your planet is gone ¯\\_(ツ)_/¯");
        }

        final List<String> events = new ArrayList<>();
        events.add(ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name());
        events.add(ESBEvent.TICK_DONE.name());
        if (events.contains(e.getPayload())) {
            planet = planetService.find(planet.getId());
            content.update(this.planet);
            content = setContent(content);
        }

        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_BUILDING_START.name())) {
            final ConstructBuildingEdit source = (ConstructBuildingEdit) e.getSource();
            final Building building = source.getBuilding();
            if (planet == null || building == null) {
                throw new NotifySBUserException("You found a zero day, congratulations!");
            }
            final Job job = jobService.createConstructionYardJob(planet.getId(), building.getId());
            if (job != null) {
                this.user = this.userService.refresh();
                this.uiEventBus.publish(source, CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name());
            }
        }

        if (e.getPayload().equals(ESBEvent.ORBITAL_CONSTRUCTION_JOB_BUILDING_START.name())) {
            final PlanetShipyardConstructionEdit source = (PlanetShipyardConstructionEdit) e.getSource();
            final Map<ShipClass, Integer> shipJobPayload = source.getShipJobPayload();
            if (planet == null) {
                throw new NotifySBUserException("You found a second zero day, congratulations!");
            }
            final Set<Job> shipyardJob = jobService.createShipyardJob(planet, shipJobPayload);
            if (!shipyardJob.isEmpty()) {
                this.user = this.userService.refresh();
                this.uiEventBus.publish(source, ORBITAL_CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name());
            }
        }
    }

    @Override
    protected void createActionSelectorMenu() {
        // Stats
        Tab dashboard = new Tab("Dashboard");
        addComponentForTabOfActionMenu(dashboard, planetDashboardDisplay);

        // Buildings
        Tab constructions = new Tab("Constructions");
        addComponentForTabOfActionMenu(constructions, planetBuildingConstructionEdit);

        // Shipyard
        Tab shipyard = new Tab("Shipyard");
        addComponentForTabOfActionMenu(shipyard, planetShipyardConstructionEdit);

        // Jobs
        Tab jobs = new Tab("Jobs");
        addComponentForTabOfActionMenu(jobs, planetJobDisplay);
        addActionListener();
    }

    @Override
    protected void addActionListener() {
        actionSelectorMenu.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            StatsLayout<Planet> componentForTab = getComponentForTabOfActionMenu(selectedTab);
            if (planet != null) {
                planet = planetService.find(planet);
            }
            componentForTab.update(planet);
            content = setContent((PlanetLayout<Planet>) componentForTab);
        });
    }

    @Override
    protected void createSubjectSelectorMenu() {
        List<Planet> allColonizedBy = planetService.findAllColonizedBy(user);
        allColonizedBy.forEach(planet -> {
            Tab planetTab = new Tab(planet.getName());
            addSubjectForTabOfSubjectMenu(planetTab, planet);
        });
        addSubjectListener();
    }

    @Override
    protected void addSubjectListener() {
        subjectSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            planet = getSubjectForTabOfSubjectMenu(selectedTab);
            if (planet != null) {
                planet = planetService.find(planet);
            }
            addSubjectForTabOfSubjectMenu(selectedTab, planet);
            content.update(planet);
            getTabForComponentOfActionMenu(content).setSelected(true);
            updateActionMenuUsability(null);
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean> readOnlyMap) {
        actionSelectorMenu.getChildren().forEach(menuItem -> ((Tab) menuItem).setEnabled(planet != null));
    }

    @Override
    protected void updateSubjectMenu() {
        List<Planet> allColonizedBy = planetService.findAllColonizedBy(user);
        subjectSelectorMenu.getChildren().forEach(component -> {
            Tab tab = (Tab) component;
            Planet subject = getSubjectForTabOfSubjectMenu(tab);
            if (!allColonizedBy.contains(subject)) {
                removeFromSubject(tab);
            }
        });

        allColonizedBy.stream().filter(shipClass -> !subjectSelectorObject.containsValue(shipClass)).forEach(planet -> {
            Tab subjectTab = new Tab(planet.getName());
            addSubjectForTabOfSubjectMenu(subjectTab, planet);
        });

        addSubjectListener();
    }
}
