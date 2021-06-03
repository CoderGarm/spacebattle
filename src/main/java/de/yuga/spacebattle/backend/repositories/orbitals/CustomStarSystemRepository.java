package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

import java.util.List;

public interface CustomStarSystemRepository {

    List<StarSystem> findAllStarSystems();
}
