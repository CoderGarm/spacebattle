package de.yuga.spacebattle.backend.services.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.buildings.BuildingRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
     * @param resourceType    what does it produces
     * @param unlockedThrough the research which unlocks this building
     * @return the new building
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Building createBuilding(@Nonnull final String name,
                                   @Nonnull final String description,
                                   final int baseValue,
                                   @Nonnull final EResourceType resourceType,
                                   @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return buildingRepository.save(new Building(name, description, baseValue, resourceType, unlockedThrough));
    }
}
