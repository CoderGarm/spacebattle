package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public class CacheStore<KEY, VALUE> {

    private final Cache<KEY, VALUE> cache;

    public CacheStore(final int expiryDuration, @Nonnull final TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "timeUnit must not be empty");

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Nullable
    public VALUE get(@Nonnull final KEY key) {
        Preconditions.checkNotNull(key, "key must not be empty");

        return cache.getIfPresent(key);
    }

    public void put(@Nonnull final KEY key, @Nonnull final VALUE value) {
        Preconditions.checkNotNull(key, "key must not be empty");
        Preconditions.checkNotNull(value, "value must not be empty");

        cache.put(key, value);
    }
}
