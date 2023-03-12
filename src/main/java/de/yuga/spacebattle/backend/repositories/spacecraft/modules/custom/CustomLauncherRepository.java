package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomLauncherRepository {

    @Nonnull
    List<Launcher> findAll();

    @Nonnull
    List<Launcher> findAllByUser(final int idUser);
}
