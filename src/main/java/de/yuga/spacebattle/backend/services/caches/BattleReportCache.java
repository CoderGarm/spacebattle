package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@Service
public class BattleReportCache {

    /**
     * Holds the last time the reports were questioned.
     */
    @Nonnull
    private final CacheStore<Integer, Tick> lastQueryBattleReportsCache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nullable
    public Tick getLastQueryBattleReports(final int idUser) {
        return lastQueryBattleReportsCache.get(idUser);
    }

    public void setLastQueryBattleReports(@Nonnull final Tick today,
                                          final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        lastQueryBattleReportsCache.put(idUser, today);
    }
}
