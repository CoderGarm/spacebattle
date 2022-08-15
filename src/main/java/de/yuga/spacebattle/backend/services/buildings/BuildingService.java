package de.yuga.spacebattle.backend.services.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.repositories.buildings.BuildingRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    @Nonnull
    private final BuildingRepository buildingRepository;

    public BuildingService(@Nonnull final BuildingRepository buildingRepository) {
        Preconditions.checkNotNull(buildingRepository, "buildingRepository shouldn't be null!");

        this.buildingRepository = buildingRepository;
    }

    @Nonnull
    public List<Building> findAll() {
        return buildingRepository.findAllBuildings();
    }

    @Nullable
    public Building find(@Nonnull final Integer idBuilding) {
        Preconditions.checkNotNull(idBuilding, "idBuilding shouldn't be null!");

        return buildingRepository.findById(idBuilding).orElse(null);
    }

    /**
     * Creates a new {@link Building}.
     *
     * @param name            the name of the planet
     * @param description     the description
     * @param baseValue       the amount of production per {@link Tick} multiplied with {@link Building#getIncreasingFactorPerLevel()}
     * @param productionType  what does it produces
     * @param educationType   the education type needed to run the building
     * @param amountOfWorkers the amount of employees
     * @param unlockedThrough the research which unlocks this building
     * @return the new building
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Building createBuilding(@Nonnull final String name,
                                   @Nonnull final String description,
                                   final int baseValue,
                                   @Nonnull final ETechLevel techLevel,
                                   @Nonnull final ProductionType productionType,
                                   @Nonnull final EEducationType educationType,
                                   final long amountOfWorkers,
                                   @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(productionType, "productionType shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        final Map<EEducationType, Long> crewRequirement = new HashMap<>();
        crewRequirement.put(educationType, amountOfWorkers);
        return buildingRepository.save(new Building(name, description, baseValue, techLevel, productionType, new CrewRequirement(crewRequirement, EDepositType.COSTS), unlockedThrough));
    }

    public List<Building> findBuildingByProductionType(@Nonnull final ProductionType productionType) {
        Preconditions.checkNotNull(productionType, "productionType shouldn't be null!");

        return buildingRepository.findBuildingsByProductionTarget(productionType);
    }

    @Nonnull
    public List<Building> findBuildingByTechLevel(@Nonnull final ETechLevel techLevel) {
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");

        final List<Building> buildingByTechLevel = buildingRepository.findBuildingByTechLevel(techLevel);
        return Objects.requireNonNullElse(buildingByTechLevel, new ArrayList<>());
    }

    /**
     * Returns the basic buildings for a newly colonized planet.
     *
     * @return the buildings
     */
    public List<Building> findBasicBuildings() {
        final Set<ProductionType> basicTypes = new HashSet<>();
        basicTypes.add(new ProductionType(EResourceType.CONSTRUCTION, EProductionCategory.PRODUCE, null));
        basicTypes.add(new ProductionType(EResourceType.CREDITS, EProductionCategory.PRODUCE, null));
        basicTypes.add(new ProductionType(EResourceType.METALORE, EProductionCategory.PRODUCE, null));
        basicTypes.add(new ProductionType(EResourceType.POPULATION, EProductionCategory.PRODUCE, null));
        basicTypes.add(new ProductionType(EResourceType.POPULATION, EProductionCategory.CAPACITY, null));
        basicTypes.add(new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_I));

        return findBuildingByTechLevel(ETechLevel.TECH_I).stream().filter(b -> basicTypes.contains(b.getProductionType())).collect(Collectors.toList());
    }
}
