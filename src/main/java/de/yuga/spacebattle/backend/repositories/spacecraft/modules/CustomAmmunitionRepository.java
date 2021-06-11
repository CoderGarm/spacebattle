package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomAmmunitionRepository {

    @Nonnull
    List<AmmunitionModule> findAll();

    @Nonnull
    List<AmmunitionModule> findAllByUser(User user);
}
