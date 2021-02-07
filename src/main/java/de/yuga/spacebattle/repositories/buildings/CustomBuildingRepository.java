package de.yuga.spacebattle.repositories.buildings;

import de.yuga.spacebattle.entities.buildings.Building;

import java.util.List;

public interface CustomBuildingRepository {

    List<Building> findAllBuildings();
}
