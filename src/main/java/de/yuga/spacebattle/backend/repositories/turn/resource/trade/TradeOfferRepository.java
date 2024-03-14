package de.yuga.spacebattle.backend.repositories.turn.resource.trade;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TradeOfferRepository extends JpaRepository<TradeOffer, Integer> {

    @Nullable
    @Query("SELECT o FROM TradeOffer o WHERE o.isDeleted = false")
    List<TradeOffer> findActiveOffers();

    @Query("SELECT CASE WHEN (o IS NOT NULL) THEN AVG(o.unitPrice) ELSE 0 END FROM TradeOffer o " +
            "WHERE o.isDeleted = :isDeleted AND o.resourceType = :resourceType AND o.tick.id >= :sinceTick AND (:exceptIdUser IS NULL OR o.seller.id != :exceptIdUser)")
    long findAveragePrice(@Param("resourceType") @Nonnull final EResourceType resourceType, final boolean isDeleted, final int sinceTick, @Nullable final Integer exceptIdUser);

    @Nullable
    @Query("SELECT o.id FROM TradeOffer o WHERE o.isDeleted = false AND o.seller.id = :idUser AND o.id = :idTradeOffer")
    Integer findActiveOffer(@Param("idUser") int idUser, @Param("idTradeOffer") int idTradeOffer);

    @Nullable
    @Query("SELECT o FROM TradeOffer o WHERE o.seller = :user")
    List<TradeOffer> forDeletionFindAllByOwner(@Nonnull final User user);
}
