package de.yuga.spacebattle.backend.repositories.events;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.events.EventRanking;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import de.yuga.spacebattle.backend.enums.events.ERankingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface EventRankingRepository extends JpaRepository<EventRanking, Integer> {

    @Nullable
    @Query("SELECT r FROM EventRanking r WHERE r.user = :user AND r.gameEvent = :gameEvent AND r.rankingCategory = :rankingCategory")
    EventRanking findFor(@Nonnull final User user, @Nonnull final EGameEvent gameEvent, @Nonnull final ERankingCategory rankingCategory);

    @Nullable
    @Query("SELECT r FROM EventRanking r WHERE r.gameEvent = :gameEvent")
    Set<EventRanking> findAll(@Nonnull final EGameEvent gameEvent);
}
