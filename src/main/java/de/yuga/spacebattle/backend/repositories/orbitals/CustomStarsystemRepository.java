package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.Starsystem;

import java.util.List;

public interface CustomStarsystemRepository {

    List<Starsystem> findAllStarsystems();
}
