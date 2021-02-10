package de.yuga.spacebattle.backend.repositories.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;

import java.util.List;

public interface CustomBuildingRepository {

    List<Building> findAllBuildings();
}
