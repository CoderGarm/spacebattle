package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomStarSystemRepository {

    @Nonnull
    List<StarSystem> findAllStarSystems();

    @Nonnull
    List<StarSystem> findAllColonizable();

    @Nonnull
    List<StarSystem> findAllColonized();
}
