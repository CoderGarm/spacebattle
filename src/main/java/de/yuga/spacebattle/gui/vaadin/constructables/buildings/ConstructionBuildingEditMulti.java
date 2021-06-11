package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingLevelDTO;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstructionBuildingEditMulti extends PlanetLayout<Planet> {

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final Map<Building, ConstructBuildingEdit> componentsMap = new HashMap<>();

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    @Nonnull
    private final ConstructionFilterEdit constructionFilterEdit = new ConstructionFilterEdit();

    public ConstructionBuildingEditMulti() {
        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);
        constructionFilterEdit.addValueChangeListener(event -> {
            final ConstructionFilterDTO filter = constructionFilterEdit.getValue();
            final Map<Boolean, List<ConstructBuildingEdit>> filteredViews = componentsMap.values().stream()
                    .collect(Collectors.partitioningBy(view -> view.fitsFilter(filter)));
            filteredViews.forEach((shouldBeDisplayed, view) -> {
                if (shouldBeDisplayed && getChildren().noneMatch(c -> c.equals(view))) {
                    this.add(view.toArray(ConstructBuildingEdit[]::new));
                } else {
                    this.remove(view.toArray(ConstructBuildingEdit[]::new));
                }
            });
        });

        add(constructionFilterEdit);
    }

    @Override
    public void updateStatistics(@Nullable final Planet planet) {
        binderPlanet.readBean(planet);
        if (planet == null) {
            clear();
            return;
        }
        if (!planet.getConstructionByResource(EResourceType.CONSTRUCTION).isEmpty()) {
            createConstructionSelection(planet);
        }
    }

    private void clear() {
        remove(componentsMap.values().toArray(ConstructBuildingEdit[]::new));
        componentsMap.clear();
    }

    /**
     * Creates the editable views with every job option by this {@link Construction}.
     *
     * @param planet the planet which can produce buildings
     */
    private void createConstructionSelection(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkArgument(planet.getOwner() != null, "planet must be colonized!");

        final User user = userService.getLoggedInUser();
        final Map<Research, Integer> researchesForUser = userService.getResearchesForUser(user);
        final Set<Building> unlockedBuildings = researchesForUser.keySet().stream()
                .map(Research::getUnlocksBuildings)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        final Set<Construction> constructions = planet.getConstructions();

        final Map<Building, Construction> constructionByBuilding = constructions.stream()
                .collect(Collectors.toMap(Construction::getBuilding, Function.identity()));

        final Map<Building, Integer> levelByBuilding = unlockedBuildings.stream()
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
            createView(planet, building, integer);
        });
        componentsMap.values().forEach(this::add);
    }

    private void createView(@Nonnull final Planet planet,
                            @Nonnull final Building building,
                            final int integer) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        final boolean setReadOnly = planet.getConstructionByResource(EResourceType.CONSTRUCTION).stream()
                .noneMatch(c -> c.getJobs().isEmpty());

        final ConstructBuildingEdit constructBuildingEdit;
        if (componentsMap.containsKey(building)) {
            constructBuildingEdit = componentsMap.get(building);
        } else {
            constructBuildingEdit = new ConstructBuildingEdit();
            componentsMap.put(building, constructBuildingEdit);
        }
        constructBuildingEdit.setValue(new BuildingLevelDTO(planet, building, integer));
        constructBuildingEdit.setReadOnly(setReadOnly);
    }
}
