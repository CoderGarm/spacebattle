package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.enums.EMissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface HeatMapRepository extends JpaRepository<HeatMap, Integer> {

    @Nullable
    @Query("SELECT h.planet FROM HeatMap h WHERE h.missionType = :missionType AND h.heat = (SELECT MAX(heat) from HeatMap  WHERE missionType = :missionType)")
    List<Planet> findHottestUsers(@Nonnull @Param("missionType") final EMissionType missionType);

    @Nullable
    @Query("SELECT h FROM HeatMap h WHERE h.planet.id IN (:planetIDs) AND h.missionType = :missionType")
    Set<HeatMap> findHeatForPlanets(@Nonnull @Param("planetIDs") final List<Integer> planetIDs, @Nonnull @Param("missionType") final EMissionType missionType);

    @Nullable
    @Query("SELECT h FROM HeatMap h WHERE h.missionType = :missionType")
    Set<HeatMap> findHeatForMissionType(@Nonnull @Param("missionType") final EMissionType missionType);

    @Nullable
    @Query("SELECT h FROM HeatMap h WHERE h.planet.id IN (:planetIDs)")
    Set<HeatMap> findHeatForPlanets(@Nonnull @Param("planetIDs") final List<Integer> planetIDs);
}
