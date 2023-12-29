package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.repositories.spacecraft.custom.CustomShipClassRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ShipClassRepository extends CrudRepository<ShipClass, Integer>, CustomShipClassRepository {

    @Nullable
    @Query("SELECT s FROM ShipClass s WHERE s.owner = :owner")
    List<ShipClass> forDeletionFindAllByOwner(@Nonnull final User owner);
}
