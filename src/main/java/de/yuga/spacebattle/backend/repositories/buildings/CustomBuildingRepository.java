package de.yuga.spacebattle.backend.repositories.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomBuildingRepository {

    @Nonnull
    List<Building> findAllBuildings();

    @Nonnull
    List<Building> findBuildingsByProductionTarget(@Nonnull ProductionType resourceType);
}
