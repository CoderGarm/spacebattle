package de.yuga.spacebattle.backend.services.events;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.events.EventRanking;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import de.yuga.spacebattle.backend.enums.events.ERankingCategory;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.repositories.events.EventRankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class RankingService {

    @Nonnull
    private final EventRankingRepository rankingRepository;

    @Autowired
    public RankingService(@Nonnull final EventRankingRepository rankingRepository) {
        this.rankingRepository = Preconditions.checkNotNull(rankingRepository, "rankingRepository must not be empty");
    }

    public void addPoints(@Nonnull final User user,
                          final int conqueredPlanets,
                          final int gainedConstructionLevels) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final EGameEvent gameEvent = EGameEvent.WAR_HARVEST_23;

        ERankingCategory rankingCategory = ERankingCategory.GAINED_PLANETS;
        EventRanking ranking = rankingRepository.findFor(user, gameEvent, rankingCategory);
        if (ranking == null) {
            ranking = new EventRanking(user, gameEvent, rankingCategory);
        }
        ranking.addPoints(conqueredPlanets);
        rankingRepository.save(ranking);

        rankingCategory = ERankingCategory.GAINED_CONSTRUCTION_LEVELS;
        ranking = rankingRepository.findFor(user, gameEvent, rankingCategory);
        if (ranking == null) {
            ranking = new EventRanking(user, gameEvent, rankingCategory);
        }
        ranking.addPoints(gainedConstructionLevels);
        rankingRepository.save(ranking);

    }

    public void addPoints(@Nonnull final User user,
                          @Nonnull final Mass tonnageDestroyed,
                          @Nonnull final Mass tonnageLoss) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(tonnageDestroyed, "tonnageDestroyed must not be empty");
        Preconditions.checkNotNull(tonnageLoss, "tonnageLoss must not be empty");

        final EGameEvent gameEvent = EGameEvent.WAR_HARVEST_23;

        ERankingCategory rankingCategory = ERankingCategory.FLEET_TONNAGE_DESTROYED;
        EventRanking ranking = rankingRepository.findFor(user, gameEvent, rankingCategory);
        if (ranking == null) {
            ranking = new EventRanking(user, gameEvent, rankingCategory);
        }
        ranking.addPoints(tonnageDestroyed.getCoordinateInMetric(EMassMetric.KT).intValue());
        rankingRepository.save(ranking);


        rankingCategory = ERankingCategory.FLEET_TONNAGE_LOST;
        ranking = rankingRepository.findFor(user, gameEvent, rankingCategory);
        if (ranking == null) {
            ranking = new EventRanking(user, gameEvent, rankingCategory);
        }
        ranking.addPoints(tonnageLoss.getCoordinateInMetric(EMassMetric.KT).intValue());
        rankingRepository.save(ranking);

    }

    @Nonnull
    public Set<EventRanking> findAll(@Nonnull final EGameEvent gameEvent) {
        Preconditions.checkNotNull(gameEvent, "gameEvent must not be empty");

        return Objects.requireNonNullElse(rankingRepository.findAll(gameEvent), new HashSet<>());
    }

    public void addPoints(@Nonnull final User winner, final boolean is1v1, final boolean is3v3, final boolean is5v5) {
        Preconditions.checkNotNull(winner, "winner must not be empty");

        final EGameEvent gameEvent = EGameEvent.TOURNAMENT_FOR_HONOR_24;

        final ERankingCategory rankingCategory = is1v1 ? ERankingCategory.WON_FIGHTS_V1 : is3v3 ? ERankingCategory.WON_FIGHTS_V3 : ERankingCategory.WON_FIGHTS_V5;
        EventRanking ranking = rankingRepository.findFor(winner, gameEvent, rankingCategory);
        if (ranking == null) {
            ranking = new EventRanking(winner, gameEvent, rankingCategory);
        }
        ranking.addPoints(1);
        rankingRepository.save(ranking);
    }
}
