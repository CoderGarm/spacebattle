package de.yuga.spacebattle.backend.repositories.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomBuildingRepository {

    List<Building> findAllBuildings();

    @Nullable
    Building findBuildingByProductionTarget(@Nonnull EResourceType resourceType);
}
