package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomPropulsionRepository {

    @Nonnull
    List<Propulsion> findAll();

    @Nonnull
    List<Propulsion> findAllByUser(User user);
}
