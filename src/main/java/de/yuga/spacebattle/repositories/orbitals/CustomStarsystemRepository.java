package de.yuga.spacebattle.repositories.orbitals;

import de.yuga.spacebattle.entities.orbitals.Starsystem;

import java.util.List;

public interface CustomStarsystemRepository {

    List<Starsystem> findAllStarsystems();
}
