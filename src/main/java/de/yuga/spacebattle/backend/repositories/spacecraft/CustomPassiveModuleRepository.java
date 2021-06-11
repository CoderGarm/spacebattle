package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomPassiveModuleRepository {

    @Nonnull
    List<PassiveModule> findAll();

    @Nonnull
    List<PassiveModule> findAllByUser(User user);
}
