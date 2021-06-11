package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;

import java.util.List;

public interface CustomShipClassRepository {

    List<ShipClass> findAllShipClasses();

    List<ShipClass> findAllShipClassesByOwner(User user);

    List<ShipClass> findAllLatestShipClassesByOwner(User user);

    ShipClass saveAndFlush(ShipClass shipClass);
}
