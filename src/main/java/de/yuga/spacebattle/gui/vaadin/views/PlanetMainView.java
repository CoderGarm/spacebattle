package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.ConstructBuildingEdit;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.SBPageTopLevelLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetBuildingConstructionEdit;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetDashboardDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetJobDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED;

@SpringComponent
@UIScope
@Route(value = PlanetMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = PlanetMainView.ROUTE, layout = MainView.class)
public class PlanetMainView extends SBPageTopLevelLayout {

    private final static Logger LOGGER = LoggerFactory.getLogger(PlanetMainView.class);

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
    private PlanetLayout content = new PlanetLayout();

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
        createSubjectSelectorMenu();
        createActionSelectorMenu();
        setContent(content);
        updateActionMenuVisibility();
    }

    /**
     * The event receiver which receives and published a job purpose.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {

        if (planet == null) {
            throw new NotifySBUserException("something went wrong - your planet has gone.");
        }

        final List<String> events = new ArrayList<>();
        events.add(ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name());
        events.add(ESBEvent.TICK_DONE.name());
        if (events.contains(e.getPayload())) {
            if (content instanceof PlanetDashboardDisplay) {
                PlanetDashboardDisplay content = (PlanetDashboardDisplay) this.content;
                Planet planet = planetService.find(this.planet.getId());
                if (planet != null) {
                    content.update(planet);
                }
                setContent(content);
            }
        }

        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_BUILDING_START.name())) {
            ConstructBuildingEdit source = (ConstructBuildingEdit) e.getSource();
            Building building = source.getBuilding();
            Job job = jobService.createConstructionYardJob(planet.getId(), building.getId());
            if (job != null) {
                this.user = this.userService.refresh();
                this.uiEventBus.publish(source, CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name());
            }
        }
    }

    @Override
    protected void createActionSelectorMenu() {
        // Stats
        actionSelectorMenu.addItem("Dashboard", event -> {
            final PlanetDashboardDisplay planetDashboardDisplay = new PlanetDashboardDisplay();
            planetDashboardDisplay.update(planet);
            setContent(planetDashboardDisplay);
        });
        // Buildings
        actionSelectorMenu.addItem("Constructions", event -> {
            final PlanetBuildingConstructionEdit planetBuildingConstructionEdit = new PlanetBuildingConstructionEdit();
            planetBuildingConstructionEdit.update(planet);
            setContent(planetBuildingConstructionEdit);
        });
        // Shipyard
        actionSelectorMenu.addItem("Shipyard", event -> {
            LOGGER.info("do shipyard stuff");
        });
        // Jobs
        actionSelectorMenu.addItem("Jobs", event -> {
            final PlanetJobDisplay planetJobDisplay = new PlanetJobDisplay();
            planetJobDisplay.update(planet);
            setContent(planetJobDisplay);
        });
    }

    @Override
    protected void createSubjectSelectorMenu() {
        List<Planet> allColonizedBy = planetService.findAllColonizedBy(user);
        allColonizedBy.forEach(planet -> {
            subjectSelectorMenu.addItem(planet.getName(), event -> {
                this.planet = planet;
                content.getPlanetResourceDisplay().update(planet);
                updateActionMenuVisibility();
            });
        });
    }

    @Override
    protected void updateActionMenuVisibility() {
        this.actionSelectorMenu.getItems().forEach(menuItem -> menuItem.setEnabled(this.planet != null));
    }

    @Override
    protected void updateMenus() {

    }

    protected void setContent(@Nonnull final PlanetLayout content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        this.content = content;
        this.content.setWidth("100%");
        super.setContent(this.content);
    }

}
