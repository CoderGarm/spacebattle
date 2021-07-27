package de.yuga.spacebattle.backend.services.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.repositories.constructables.buildings.ConstructionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConstructionService {

    @Nonnull
    private final ConstructionRepository constructionRepository;

    @Autowired
    public ConstructionService(@Nonnull final ConstructionRepository constructionRepository) {
        Preconditions.checkNotNull(constructionRepository, "constructionRepository shouldn't be null!");

        this.constructionRepository = constructionRepository;
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

    /**
     * Returns every building which could be build or upgraded with ne next level.
     *
     * @param planet            the planet
     * @param researchesForUser the researches of the logged in user
     * @return the list of possible constructions
     */
    @Nonnull
    public Map<Building, Integer> getUpgradeableConstructions(final Planet planet, Map<Research, Integer> researchesForUser) {

        final Set<Building> unlockedBuildings = researchesForUser.keySet().stream()
                .map(Research::getUnlocksBuildings)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        final Set<Construction> constructions = planet.getConstructions();

        final Map<Building, Construction> constructionByBuilding = constructions.stream()
                .collect(Collectors.toMap(Construction::getBuilding, Function.identity()));

        return unlockedBuildings.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        building -> constructionByBuilding.containsKey(building) ? constructionByBuilding.get(building).getLevel() + 1 : 1));
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
}
