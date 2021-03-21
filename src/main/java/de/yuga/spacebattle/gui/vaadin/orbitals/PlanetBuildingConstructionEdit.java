package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.ConstructBuildingEdit;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.BuildingLevelWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlanetBuildingConstructionEdit extends PlanetLayout {

    @Nonnull
    private final Map<Building, ConstructBuildingEdit> componentsMap = new HashMap<>();

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    public PlanetBuildingConstructionEdit() {

        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);

        Label l = new Label("planet constructions");
        add(l);
    }

    public void update(@Nullable final Planet planet) {
        binderPlanet.readBean(planet);
        if (planet == null) {
            clear();
            return;
        }
        planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.CONSTRUCTION)
                .findFirst()
                .ifPresentOrElse(this::createConstructionSelection, this::clear);
    }

    private void clear() {
        componentsMap.values().forEach(this::remove);
        componentsMap.clear();
    }

    /**
     * Creates the editable views with every job option by this {@link Construction}.
     *
     * @param construction the construction which includes the job options
     */
    private void createConstructionSelection(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");
        User user = construction.getPlanet().getOwner();
        if (user == null) {
            throw new NotifySBUserException("You should be logged in here.");
        }
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

        final HashSet<Building> oldBuildings = new HashSet<>(componentsMap.keySet());
        final HashSet<Building> newBuildings = new HashSet<>(levelByBuilding.keySet());
        oldBuildings.removeAll(newBuildings);
        oldBuildings.forEach(building -> {
            remove(componentsMap.get(building));
            componentsMap.remove(building);
        });

        levelByBuilding.forEach((building, integer) -> {
            final ConstructBuildingEdit constructBuildingEdit;
            if (componentsMap.containsKey(building)) {
                constructBuildingEdit = componentsMap.get(building);
            } else {
                constructBuildingEdit = new ConstructBuildingEdit();
                componentsMap.put(building, constructBuildingEdit);
            }
            constructBuildingEdit.setValue(new BuildingLevelWrapper(building, integer));
        });
        componentsMap.values().forEach(this::add);
    }
}
