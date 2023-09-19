package de.yuga.spacebattle.backend.repositories.turn.mission;

import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMission;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ConvoyProtectionMissionRepository extends JpaRepository<ConvoyProtectionMission, Integer> {

    @Nullable
    @Query("SELECT m FROM ConvoyProtectionMission  m WHERE m.actor.id = :idUser AND m.isDeleted = false")
    List<ConvoyProtectionMission> findAllForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT m FROM ConvoyProtectionMission  m WHERE m.protectedTrade.id IN (:tradedResourcesIDs) AND m.isDeleted = false")
    List<ConvoyProtectionMission> findConvoyProtectionForTrades(@Param("tradedResourcesIDs") final Collection<Integer> tradedResourcesIDs);

    @Nullable
    @Query("SELECT DISTINCT t FROM TradedResource t " +
            "WHERE (t.buyer.id = :idUser OR t.tradeOffer.seller.id = :idUser) " +
            "AND t.tick.id = :tickNo " +
            "AND t NOT IN (SELECT m.protectedTrade FROM ConvoyProtectionMission m WHERE m.actor.id = :idUser AND m.isDeleted = false)")
    Set<TradedResource> findAllConvoysWithoutEscort(@Param("tickNo") final int tickNo, @Param("idUser") final int idUser);
}
