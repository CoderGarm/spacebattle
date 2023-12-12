package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.TransportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TransportJobRepository extends JpaRepository<TransportJob, Integer> {

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = false AND :warship MEMBER OF t.ships")
    TransportJob findByWarship(@Nonnull final WarShip warship);

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = false AND t.ticksLeft = 1")
    List<TransportJob> findAllForToday();

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = false")
    List<TransportJob> findAllPending();

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = false AND t.owner.id = :idUser AND t.origin.id = :idPlanet")
    List<TransportJob> findAllFor(final int idUser, final int idPlanet);

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = true AND t.finished.id = :tickNo AND t.owner.id = :idUser")
    List<TransportJob> findFinishedFor(final int tickNo, final int idUser);

    @Nullable
    @Query("SELECT t FROM TransportJob t WHERE t.isDeleted = false AND t.origin.id = :idOrigin AND t.destination.id = :idDestination")
    TransportJob findByOriginAndDestination(final int idOrigin, final int idDestination);
}
