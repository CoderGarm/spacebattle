package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface WarShipRepository extends CrudRepository<WarShip, Integer> {

    @Nullable
    @Query("SELECT w FROM WarShip w WHERE w.shipyard.id = :idPlanet AND w.isDeleted = false AND w.isOperational = false")
    List<WarShip> findAliveInoperationalForPlanet(@Param("idPlanet") final int idPlanet);

    @Nullable
    @Query("SELECT w FROM WarShip w WHERE w.isDeleted = false AND w.isOperational = true AND w.detachment.fleet IS NULL AND w.detachment.mission IS NULL AND w.shipClass.owner.id = :idUser")
    Set<WarShip> findPooledShipsByUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT w FROM WarShip w WHERE  w.isDeleted = false AND w.isOperational = true AND w.shipClass.owner.id = :idUser")
    Set<WarShip> findShipsByUser(int idUser);
}
