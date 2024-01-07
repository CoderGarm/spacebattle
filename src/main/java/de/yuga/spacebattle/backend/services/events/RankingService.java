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
}
