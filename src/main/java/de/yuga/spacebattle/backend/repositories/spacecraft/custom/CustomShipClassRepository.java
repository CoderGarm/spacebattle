package de.yuga.spacebattle.backend.repositories.spacecraft.custom;

import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomShipClassRepository {

    List<ShipClass> findAllShipClasses();

    List<ShipClass> findAllShipClassesByOwner(@Nonnull User user);

    List<ShipClass> findAllLatestShipClassesByOwner(@Nonnull Owner user);

    boolean checkIfClassNameIsFree(int idOwner, @Nonnull String className);
}
