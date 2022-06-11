package de.yuga.spacebattle.backend.repositories.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface BuildingRepository extends CrudRepository<Building, Integer>, CustomBuildingRepository {

    @Nullable
    @Query("SELECT b FROM Building b WHERE b.techLevel = :techLevel")
    List<Building> findBuildingByTechLevel(@Nonnull @Param("techLevel") ETechLevel techLevel);
}
