package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.account.User;
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
    @Query("SELECT h.owner FROM HeatMap h WHERE h.owner.dType = de.yuga.spacebattle.backend.enums.OwnerType.USER AND h.missionType = :missionType AND h.heat = (SELECT MAX(heat) from HeatMap  WHERE missionType = :missionType)")
    List<User> findHottestUsers(@Nonnull @Param("missionType") final EMissionType missionType);

    @Nullable
    @Query("SELECT h FROM HeatMap h WHERE h.owner.id IN (:userIDs) AND h.missionType = :missionType")
    Set<HeatMap> findHeatForUser(@Nonnull @Param("userIDs") final List<Integer> userIDs, @Nonnull @Param("missionType") final EMissionType missionType);
}
