package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import java.util.List;

public interface CustomHullRepository {

    List<Hull> findAllHulls();
}
