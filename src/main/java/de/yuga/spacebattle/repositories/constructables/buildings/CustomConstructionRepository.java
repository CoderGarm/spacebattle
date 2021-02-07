package de.yuga.spacebattle.repositories.constructables.buildings;

import de.yuga.spacebattle.entities.constructables.buildings.Construction;

import java.util.List;

public interface CustomConstructionRepository {

    List<Construction> findAllConstructions();
}
