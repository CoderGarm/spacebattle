package de.yuga.spacebattle.backend.repositories.turn.resource.trade;

import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TradedResourceRepository extends JpaRepository<TradedResource, Integer> {

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE t.tick.id IN (:timeframe)")
    List<TradedResource> findForTicks(@Param("timeframe") @Nonnull final List<Integer> timeframe);

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE (t.buyer.id = :idUser OR t.tradeOffer.seller.id = :idUser) AND (t.finished.id = :tickNo OR t.finished IS NULL)")
    List<TradedResource> findFinishedAndPendingTradesForUser(@Param("tickNo") final int tickNo, @Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE (t.buyer.id = :idUser OR t.tradeOffer.seller.id = :idUser) AND (t.finished.id = :tickNo)")
    List<TradedResource> findFinishedForUser(@Param("tickNo") final int tickNo, @Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE (t.buyer.id = :idUser OR t.tradeOffer.seller.id = :idUser) AND t.tick.id = :tickNo")
    List<TradedResource> findTradesStartedAtTickForUser(@Param("tickNo") final int tickNo, @Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE t.isDeleted = false")
    List<TradedResource> findAllUnfinished();

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE t.ticksLeft = 1 OR t.tick.id = :tickNo")
    List<TradedResource> findTomorrowsTrades(@Param("tickNo") final int tickNo);

    @Nullable
    @Query("SELECT t FROM TradedResource t WHERE t.finished.id = :tickNo AND t.isDeleted = true")
    List<TradedResource> findTodayTrades(@Param("tickNo") final int tickNo);
}
