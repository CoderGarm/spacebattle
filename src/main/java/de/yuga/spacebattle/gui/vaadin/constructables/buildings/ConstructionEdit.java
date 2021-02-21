package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_STARTED;

public class ConstructionEdit extends HorizontalLayout {

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final JobService jobService = ViewHelper.getService(JobService.class);

    @Nonnull
    private User user;

    @Nonnull
    private final Planet planet;

    @Nonnull
    private Component content = new Label();

    /**
     * Creates an editable view for every job which could be done by this construction.
     *
     * @param construction the construction
     */
    public ConstructionEdit(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        user = userService.isLoggedIn();
        if (user == null) {
            throw new NotifySBUserException("I shall not pass!");
        }
        this.uiEventBus.subscribe(this);
        this.planet = construction.getPlanet();

        Building building = construction.getBuilding();
        int level = construction.getLevel();
        BuildingDisplay buildingDisplay = new BuildingDisplay(building);
        Label levelValue = new Label("Level: " + level);
        add(levelValue, buildingDisplay);

        EResourceType resourceType = construction.getBuilding().getResourceType();
        switch (resourceType) {
            case CONSTRUCTION:
                createConstructionSelection(construction);
                break;
            case ORBITALCONSTRUCTION:
            case RESEARCH:
            default:
                break;
        }
    }

    /**
     * Creates the editable views with every job option by this {@link Construction}.
     *
     * @param construction the construction which includes the job options
     */
    private void createConstructionSelection(@Nonnull Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        Set<Building> unlockedBuildings = user.getResearches().keySet().stream()
                .map(Research::getUnlocksBuildings)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Construction> constructions = construction.getPlanet().getConstructions();

        Map<Building, Construction> constructionByBuilding = constructions.stream()
                .collect(Collectors.toMap(Construction::getBuilding, Function.identity()));

        Map<Building, Integer> levelByBuilding = unlockedBuildings.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        building -> constructionByBuilding.containsKey(building) ? constructionByBuilding.get(building).getLevel() + 1 : 1));

        MenuBar menuBar = new MenuBar();
        menuBar.addItem("Build something", event -> {
            remove(content);
            content = new ConstructBuildingEditMulti(levelByBuilding);
            add(content);
        });
        add(menuBar);
    }

    /**
     * The event receiver which receives and published a job purpose.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.CONSTRUCT_BUILDING.name())) { // todo buttons fire twice? #3 event fired twice?
            ConstructBuildingEdit source = (ConstructBuildingEdit) e.getSource();
            Building building = source.getBuilding();
            Job job = jobService.createConstructionYardJob(planet.getId(), building.getId());
            if (job != null) {
                this.user = this.userService.refresh();
                this.uiEventBus.publish(source, CONSTRUCTION_JOB_STARTED.name());
            }
        }

    }
}
