package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.ConstructionEditMulti;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetResourceDisplay;
import de.yuga.spacebattle.gui.vaadin.turn.JobDisplayMulti;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Route(value = PlanetMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = PlanetMainView.ROUTE, layout = MainView.class)
public class PlanetMainView extends VerticalLayout {

    @Nonnull
    public static final String ROUTE = "planets";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final User user;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final MenuBar subjectSelectorMenu;

    @Nonnull
    private final MenuBar actionSelectorMenu;

    @Nullable
    private Planet planet;

    @Nonnull
    private final Label currentSelectionName = new Label();

    @Nonnull
    private Component content = new Label();

    @Autowired
    public PlanetMainView(@Nonnull final UserService userService,
                          @Nonnull final PlanetService planetService,
                          @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.planetService = planetService;
        User loggedIn = userService.isLoggedIn();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;
        this.subjectSelectorMenu = createSubjectSelectorMenu();
        add(this.subjectSelectorMenu);
        this.actionSelectorMenu = createActionSelectorMenu();
        add(this.actionSelectorMenu);
        add(currentSelectionName);
        add(content);
        updateMenu();
    }

    /**
     * The event receiver which receives and published a job purpose.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        final List<String> events = new ArrayList<>();
        events.add(ESBEvent.CONSTRUCTION_JOB_STARTED.name());
        events.add(ESBEvent.TICK.name());


        if (events.contains(e.getPayload())) { // todo buttons fire twice? #4 event fired twice?
            if (content instanceof PlanetResourceDisplay && planet != null) {
                PlanetResourceDisplay content = (PlanetResourceDisplay) this.content;
                Planet planet = planetService.find(this.planet.getId());
                if (planet != null) { // should be never null here
                    content.updatePlanet(planet);
                }
            }
        }
    }

    private MenuBar createActionSelectorMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("actionSelectorMenu");
        menuBar.setWidth("100%");

        // Stats
        menuBar.addItem("Stats", event -> {
            remove(content);
            content = new PlanetResourceDisplay(planet);
            add(content);
        });
        // Buildings
        menuBar.addItem("Constructions", event -> {
            remove(content);
            content = new ConstructionEditMulti(planet.getConstructions());
            add(content);
        });
        // Jobs
        menuBar.addItem("Jobs", event -> {
            Set<Job> jobSet = planet.getConstructions().stream()
                    .filter(construction -> construction.getJob() != null)
                    .map(Construction::getJob)
                    .collect(Collectors.toSet());
            remove(content);
            content = new JobDisplayMulti(jobSet);
            add(content);
        });
        return menuBar;
    }

    private void updateMenu() {
        this.actionSelectorMenu.getItems().forEach(menuItem -> menuItem.setEnabled(this.planet != null));
    }

    private MenuBar createSubjectSelectorMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("subjectSelectorMenu");
        menuBar.setWidth("100%");
        List<Planet> allColonizedBy = planetService.findAllColonizedBy(user);
        allColonizedBy.forEach(planet -> {

            menuBar.addItem(planet.getName(), event -> {
                this.planet = planet;
                currentSelectionName.setText(planet.getName());
                updateMenu();
            });
        });
        return menuBar;
    }

}
