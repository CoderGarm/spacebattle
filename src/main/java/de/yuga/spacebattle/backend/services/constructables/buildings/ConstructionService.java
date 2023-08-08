package de.yuga.spacebattle.backend.services.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.repositories.constructables.buildings.ConstructionRepository;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConstructionService {

    @Nonnull
    private final ConstructionRepository constructionRepository;

    @Nonnull
    private final BuildingService buildingService;

    @Autowired
    public ConstructionService(@Nonnull final ConstructionRepository constructionRepository, @Nonnull final BuildingService buildingService) {
        this.constructionRepository = Preconditions.checkNotNull(constructionRepository, "constructionRepository must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
    }

    @Nonnull
    public List<Construction> findAll() {
        return constructionRepository.findAllConstructions();
    }

    @Nullable
    public Construction find(@Nonnull final Integer idConstruction) {
        Preconditions.checkNotNull(idConstruction, "idConstruction shouldn't be null!");

        return constructionRepository.findById(idConstruction).orElse(null);
    }

    public Construction save(@Nonnull final Construction entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return constructionRepository.save(entity);
    }

    public void delete(@Nonnull final Construction entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        constructionRepository.delete(entity);
    }

    /**
     * Returns every constructed building on this planet.
     *
     * @param idPlanet this planet
     * @return the constructions
     */
    @Nonnull
    public List<Construction> findAllConstructionsOnPlanet(final int idPlanet) {
        return constructionRepository.findAllConstructionsOnPlanet(idPlanet);
    }

    @Nonnull
    public Set<Construction> getUpgradeableConstructions(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(planet.getOwner(), "planet owner must not be empty");

        final List<Building> unlockedBuildings = buildingService.findAllByUser(planet.getOwner().getId());

        final Set<Construction> constructions = planet.getConstructions();

        final Map<Building, Construction> constructionByBuilding = constructions.stream()
                .collect(Collectors.toMap(Construction::getBuilding, Function.identity()));

        final HashSet<Construction> result = new HashSet<>(constructionByBuilding.values());
        unlockedBuildings.forEach(building -> {
            if (!constructionByBuilding.containsKey(building)) {
                result.add(new Construction(planet, building, 0));
            }
        });

        return result;
    }

    /**
     * Returns the costs of a planned building corresponding to its target level.
     *
     * @param idBuilding  the id of the building
     * @param targetLevel the targeted level
     * @return the costs
     */
    @Nullable
    public ResourceDeposit getCosts(final int idBuilding, final int targetLevel) {

        final ResourceDeposit costs = constructionRepository.getCosts(idBuilding);
        if (costs == null) {
            return null;
        }
        return JobCostsCalculator.getCostsForLevel(costs, targetLevel);
    }

    @Nonnull
    public Set<Construction> saveAll(@Nonnull final Collection<Construction> constructions) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");

        final Iterable<Construction> c = constructionRepository.saveAll(constructions);
        return StreamSupport.stream(c.spliterator(), false).collect(Collectors.toSet());
    }

    @Nonnull
    public List<Construction> findInoperationalForUser(final int idUser) {
        return Objects.requireNonNullElse(constructionRepository.findInoperationalForUser(idUser), new ArrayList<>());
    }

    @Nonnull
    public List<Construction> findInoperationalForPlanet(final int idPlanet) {
        return Objects.requireNonNullElse(constructionRepository.findInoperationalForPlanet(idPlanet), new ArrayList<>());
    }

    @Nonnull
    public List<Construction> findAllConstructionsForUser(final int idUser) {
        return Objects.requireNonNullElse(constructionRepository.findAllConstructionsForUser(idUser), new ArrayList<>());
    }
}
