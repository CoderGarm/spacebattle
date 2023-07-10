package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface ConvoyProtectionMissionRepository extends JpaRepository<ConvoyProtectionMission, Integer> {


    @Nullable
    @Query("SELECT m FROM ConvoyProtectionMission  m WHERE m.actor.id = :idUser AND m.isDeleted = false")
    List<ConvoyProtectionMission> findAllForUser(@Param("idUser") final int idUser);
}
