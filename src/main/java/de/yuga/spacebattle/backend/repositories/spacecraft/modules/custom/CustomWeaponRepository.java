package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomWeaponRepository {

    @Nonnull
    List<Weapon> findAll();

    @Nonnull
    List<Weapon> findAllByUser(User user);
}
