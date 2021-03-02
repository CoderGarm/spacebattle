package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomModuleRepository {

    @Nonnull
    List<Module> findAll();

    @Nonnull
    List<Module> findAllByUser(User user);
}
