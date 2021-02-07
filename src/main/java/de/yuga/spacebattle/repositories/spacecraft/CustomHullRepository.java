package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Hull;

import java.util.List;

public interface CustomHullRepository {

    List<Hull> findAllHulls();
}
