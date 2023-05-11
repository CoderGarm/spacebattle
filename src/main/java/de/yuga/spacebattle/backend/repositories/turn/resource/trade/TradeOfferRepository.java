package de.yuga.spacebattle.backend.repositories.turn.resource.trade;

import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nullable;
import java.util.List;

public interface TradeOfferRepository extends JpaRepository<TradeOffer, Integer> {

    @Nullable
    @Query("SELECT o FROM TradeOffer  o WHERE o.isDeleted = false")
    List<TradeOffer> findActiveOffers();
}
