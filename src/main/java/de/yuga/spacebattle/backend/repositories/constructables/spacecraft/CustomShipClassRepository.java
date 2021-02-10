package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;

import java.util.List;

public interface CustomShipClassRepository {

    List<ShipClass> findAllShipClasses();
}
