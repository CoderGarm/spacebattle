package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarShipRepository extends CrudRepository<WarShip, Integer> {

    @Query("SELECT w FROM WarShip w WHERE w.shipyard.id = :idPlanet AND w.isDeleted = false AND w.isOperational = false")
    List<WarShip> findAliveInoperationalForPlanet(@Param("idPlanet") final int idPlanet);
}
