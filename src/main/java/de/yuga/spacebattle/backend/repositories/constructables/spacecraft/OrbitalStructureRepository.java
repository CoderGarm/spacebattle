package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

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
    @Query("SELECT s FROM OrbitalStructure s WHERE s.owner.id = :idOwner AND s.orbit.system.id IN (:starSystemIDs)")
    List<OrbitalStructure> findAllBySystemForUser(@Nonnull final Collection<Integer> starSystemIDs, final int idOwner);
}
