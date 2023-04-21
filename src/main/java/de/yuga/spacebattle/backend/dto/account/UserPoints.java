package de.yuga.spacebattle.backend.dto.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;

public class UserPoints {

    @Nonnull
    private final User user;

    public UserPoints(@Nonnull final User user) {
        this.user = Preconditions.checkNotNull(user, "user must not be empty");
    }
}
