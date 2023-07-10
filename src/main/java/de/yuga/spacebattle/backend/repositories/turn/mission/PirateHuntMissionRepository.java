package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface PirateHuntMissionRepository extends JpaRepository<PirateHuntMission, Integer> {


    @Nullable
    @Query("SELECT m FROM PirateHuntMission  m WHERE m.actor.id = :idUser AND m.isDeleted = false")
    List<PirateHuntMission> findAllForUser(@Param("idUser") final int idUser);
}
