package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface OrbitalStructureRepository extends JpaRepository<OrbitalStructure, Integer> {

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.orbit.planet.id = :idPlanet")
    List<OrbitalStructure> findByPlanet(final int idPlanet);

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.orbit.system.id IN (:starSystemIDs)")
    List<OrbitalStructure> findAllBySystem(@Nonnull final Collection<Integer> starSystemIDs);

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.owner.id = :idOwner")
    List<OrbitalStructure> findAllForUser(final int idOwner);

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.orbit.planet.id = :idPlanet AND s.isDeleted = false AND s.isOperational = false")
    List<OrbitalStructure> findAliveInoperationalForPlanet(final int idPlanet);

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.owner = :user AND s.isOperational = true")
    List<OrbitalStructure> findAllByOwner(@Nonnull final User user);

    @Nullable
    @Query("SELECT s FROM OrbitalStructure s WHERE s.owner = :user")
    List<OrbitalStructure> forDeletionFindAllByOwner(@Nonnull final User user);
}
