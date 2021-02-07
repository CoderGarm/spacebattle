package de.yuga.spacebattle.repositories.constructables.spacecraft;

import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;

import java.util.List;

public interface CustomShipClassRepository {

    List<ShipClass> findAllShipClasses();
}
