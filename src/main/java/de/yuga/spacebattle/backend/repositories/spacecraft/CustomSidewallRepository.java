package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomSidewallRepository {

    @Nonnull
    List<Sidewall> findAll();

    @Nonnull
    List<Sidewall> findAllByUser(User user);
}
