package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomArmorRepository {

    @Nonnull
    List<Armor> findAll();

    @Nonnull
    List<Armor> findAllByUser(final int idUser);
}
