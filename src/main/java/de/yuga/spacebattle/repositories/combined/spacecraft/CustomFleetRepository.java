package de.yuga.spacebattle.repositories.combined.spacecraft;

import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;

import java.util.List;

public interface CustomFleetRepository {

    List<Fleet> findAllFleets();
}
