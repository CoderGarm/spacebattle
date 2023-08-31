package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import org.apache.commons.lang3.StringUtils;
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
    private final CacheStore<Integer, Integer> lastQueryBattleReportsCache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    public BattleReportCache(@Nonnull final CacheFileWriter cacheFileWriter) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
    }

    @Nullable
    public Integer getLastQueryBattleReports(final int idUser) {

        Integer lastMentionedTick = lastQueryBattleReportsCache.get(idUser);
        if (lastMentionedTick == null) {
            lastMentionedTick = cacheFileWriter.getFileCacheContent(this.getClass())
                    .keySet().stream()
                    .filter(k -> k.startsWith(idUser + "-"))
                    .map(k -> k.split("-")[1])
                    .filter(StringUtils::isNotBlank)
                    .map(Integer::parseInt)
                    .sorted(Integer::compareTo)
                    .reduce((o1, o2) -> o2)
                    .orElse(null);
        }
        return lastMentionedTick;
    }

    public void setLastQueryBattleReports(@Nonnull final Tick today,
                                          final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        lastQueryBattleReportsCache.put(idUser, today.getNo());
        cacheFileWriter.writeToFile(this.getClass(), idUser + "|" + today.getNo(), "random");
    }
}
