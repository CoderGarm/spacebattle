package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.TransportJob;
import de.yuga.spacebattle.backend.repositories.turn.TransportJobRepository;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TransportJobService {

    @Nonnull
    private final TransportJobRepository transportJobRepository;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Autowired
    public TransportJobService(@Nonnull final TransportJobRepository transportJobRepository,
                               @Nonnull final PlanetService planetService,
                               @Nonnull final WarShipService warShipService,
                               @Nonnull final TickTimeService tickTimeService) {
        this.transportJobRepository = Preconditions.checkNotNull(transportJobRepository, "transportJobRepository must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
    }

    @Nonnull
    public TransportJob save(@Nonnull final TransportJob entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return transportJobRepository.save(entity);
    }

    @Nullable
    public TransportJob transferPooledWarship(final int idUser, final int idWarship, final int idPlanetDestination) {
        final WarShip warShip = warShipService.findById(idWarship);
        if (warShip == null) {
            return null;
        }
        final Owner owner = warShip.getShipClass().getOwner();
        if (owner.getId() != idUser) {
            return null;
        }

        final Planet destination = planetService.find(idPlanetDestination);
        if (destination == null || destination.getOwner() == null || destination.getOwner().getId() != idUser) {
            return null;
        }

        if (warShip.getMothball() == null && warShip.getTransportJob() != null && destination.equals(warShip.getTransportJob().getOrigin())) {
            // if the ship is set back to its origin
            clearTransportBackToMothball(warShip, destination);
            return null;
        }

        final Planet origin = warShip.getMothball() != null ? warShip.getMothball() : (warShip.getTransportJob() != null ? warShip.getTransportJob().getOrigin() : null);
        Preconditions.checkNotNull(origin, "origin must not be empty");

        if (destination.equals(origin)) {
            // nothing happens
            return null;
        }

        final Tick today = tickTimeService.getToday();
        TransportJob transportJob = findByOriginAndDestination(origin, destination);
        if (transportJob == null) {
            transportJob = findByWarship(warShip);
            if (transportJob != null
                    && transportJob.getShips().stream().allMatch(w -> w.equals(warShip))
                    && transportJob.getTick().equals(today)) {
                // if only this ship is inside, just amend existing
                transportJob.setDestination(destination);
            }
        }

        if (transportJob == null) {
            // create if not present or job is not unique for the ship
            final int timeToTravel = DistanceCalculator.getTimeToTravel(origin, destination);
            transportJob = new TransportJob(today, destination, warShip, timeToTravel);
        }

        transportJob = save(transportJob);

        warShip.setTransportJob(transportJob);
        warShipService.save(warShip);
        return transportJob;
    }

    private void clearTransportBackToMothball(@Nonnull final WarShip warShip,
                                              @Nonnull final Planet destination) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final TransportJob transportJob = warShip.getTransportJob();
        if (transportJob != null) {
            transportJob.getShips().remove(warShip);
            if (transportJob.getShips().isEmpty()) {
                transportJobRepository.delete(transportJob);
            } else {
                save(transportJob);
            }

            warShip.setMothball(destination);
            warShipService.save(warShip);
        }
    }

    @Nullable
    private TransportJob findByOriginAndDestination(@Nonnull final Planet origin, @Nonnull final Planet destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        return transportJobRepository.findByOriginAndDestination(origin.getId(), destination.getId());
    }

    @Nullable
    private TransportJob findByWarship(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        return transportJobRepository.findByWarship(warShip);
    }

    public void finishAll(@Nonnull final Tick today, @Nonnull final List<TransportJob> transportJobs) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(transportJobs, "transportJobs must not be empty");

        transportJobs.forEach(t -> t.setFinished(today));
        transportJobRepository.saveAll(transportJobs);
    }

    @Nonnull
    public List<TransportJob> findAllForToday() {
        return Objects.requireNonNullElse(transportJobRepository.findAllForToday(), new ArrayList<>());
    }

    @Nonnull
    public List<TransportJob> findAllFor(final int idUser, final int idPlanet) {
        return Objects.requireNonNullElse(transportJobRepository.findAllFor(idUser, idPlanet), new ArrayList<>());
    }

    @Nonnull
    public List<TransportJob> findFinishedFor(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(transportJobRepository.findFinishedFor(today.getNo(), idUser), new ArrayList<>());
    }

    public void deleteAll(@Nonnull final Set<TransportJob> toRemove) {
        Preconditions.checkNotNull(toRemove, "toRemove must not be empty");

        transportJobRepository.deleteAll(toRemove);
    }
}
