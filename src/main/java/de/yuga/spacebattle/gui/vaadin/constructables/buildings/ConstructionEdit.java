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
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstructionEdit extends HorizontalLayout {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConstructionEdit.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final User user;

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
    private void createConstructionSelection(@Nonnull final Construction construction) {
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
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }
}
