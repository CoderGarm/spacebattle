package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface PirateHuntMissionRepository extends JpaRepository<PirateHuntMission, Integer> {

    @Nullable
    @Query("SELECT DISTINCT m FROM PirateHuntMission m WHERE m.actor.id = :idUser AND m.isDeleted = false")
    List<PirateHuntMission> findAllForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT DISTINCT m FROM PirateHuntMission m WHERE m.venue = :planet AND m.isDeleted = false")
    List<PirateHuntMission> findAllForPlanet(@Param("planet") @Nonnull final Planet planet);

    @Nullable
    @Query("SELECT DISTINCT m FROM PirateHuntMission m WHERE m.venue IN (:planets) AND m.isDeleted = false")
    List<PirateHuntMission> findAllForPlanets(@Param("planets") @Nonnull final Set<Planet> planets);

}
