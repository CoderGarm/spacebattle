package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomUserRepository {

    @Nonnull
    List<User> findAllUsers();

    @Nullable
    User login(@Nonnull final String username, @Nonnull final String password);

    @Nullable
    User checkParameter(@Nonnull final String username, @Nonnull final String email);
}
