package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomSidewallRepository {

    @Nonnull
    List<Sidewall> findAll();

    @Nonnull
    List<Sidewall> findAllByUser(final int idUser);
}
