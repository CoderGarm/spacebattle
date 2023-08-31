package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;

public class BaseCache {

    @Nonnull
    protected String getCacheKey(@Nonnull final Tick today,
                                 @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        return user.getId() + "-" + today.getId();
    }

    @Nonnull
    protected String getCacheKey(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return idUser + "-" + today.getId();
    }

    public int getUserId(@Nonnull final String key) {
        Preconditions.checkNotNull(key, "key must not be empty");

        return Integer.parseInt(key.split("-")[0]);
    }

    public int getTickId(@Nonnull final String key) {
        Preconditions.checkNotNull(key, "key must not be empty");

        return Integer.parseInt(key.split("-")[1]);
    }
}
