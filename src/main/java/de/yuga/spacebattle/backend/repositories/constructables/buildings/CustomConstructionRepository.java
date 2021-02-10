package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;

import java.util.List;

public interface CustomConstructionRepository {

    List<Construction> findAllConstructions();
}
