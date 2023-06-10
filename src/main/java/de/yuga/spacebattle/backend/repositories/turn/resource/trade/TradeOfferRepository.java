package de.yuga.spacebattle.backend.repositories.turn.resource.trade;

import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TradeOfferRepository extends JpaRepository<TradeOffer, Integer> {

    @Nullable
    @Query("SELECT o FROM TradeOffer  o WHERE o.isDeleted = false")
    List<TradeOffer> findActiveOffers();

    @Nullable
    @Query("SELECT o FROM TradeOffer o WHERE o.resourceType = :resourceType ORDER BY o.tick.id desc")
    List<TradeOffer> findLatestOffer(@Param("resourceType") @Nonnull final EResourceType resourceType, @Nonnull final Pageable pageable);
}
