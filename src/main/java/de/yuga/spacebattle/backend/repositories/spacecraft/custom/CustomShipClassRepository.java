package de.yuga.spacebattle.backend.repositories.spacecraft.custom;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomShipClassRepository {

    List<ShipClass> findAllShipClasses();

    List<ShipClass> findAllShipClassesByOwner(@Nonnull User user);

    @Nonnull
    List<ShipClass> findAllLatestShipClassesByOwner(final int idUser);

    boolean checkIfClassNameIsFree(int idOwner, @Nonnull String className);
}
