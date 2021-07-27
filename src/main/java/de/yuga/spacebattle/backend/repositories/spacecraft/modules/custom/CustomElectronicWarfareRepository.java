package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomElectronicWarfareRepository {

    @Nonnull
    List<ElectronicWarfare> findAll();

    @Nonnull
    List<ElectronicWarfare> findAllByUser(User user);
}
