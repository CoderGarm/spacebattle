package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.WarShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class WarShipService {

    @Nonnull
    private final WarShipRepository warShipRepository;

    @Autowired
    public WarShipService(@Nonnull final WarShipRepository warShipRepository) {
        Preconditions.checkNotNull(warShipRepository, "warShipRepository shouldn't be null!");

        this.warShipRepository = warShipRepository;
    }

    @Nullable
    public WarShip findById(int idWarShip) {
        return warShipRepository.findById(idWarShip).orElse(null);
    }

    @Nonnull
    public List<WarShip> findByIds(Collection<Integer> ids) {
        final Iterable<WarShip> allById = warShipRepository.findAllById(ids);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    public WarShip find(@Nonnull final WarShip fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return warShipRepository.findById(fleet.getId()).orElse(null);
    }

    public Set<WarShip> findAll() {
        final Iterable<WarShip> allById = warShipRepository.findAll();
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toSet());
    }

    public WarShip save(@Nonnull final WarShip fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return warShipRepository.save(fleet);
    }

    public Collection<WarShip> saveAll(@Nonnull final Collection<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        final Iterable<WarShip> storedWarShips = warShipRepository.saveAll(warShips);
        return StreamSupport.stream(storedWarShips.spliterator(), false).collect(Collectors.toList());
    }

    public void markAsDestroyed(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        markAllAsDestroyed(List.of(warShip));
    }

    public void markAllAsDestroyed(@Nonnull final Collection<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        warShips.forEach(WarShip::delete);
        warShipRepository.saveAll(warShips);
    }

    @Nonnull
    public List<WarShip> findAliveInoperationalForPlanet(final int idPlanet) {
        return Objects.requireNonNullElse(warShipRepository.findAliveInoperationalForPlanet(idPlanet), new ArrayList<>());
    }

    @Nonnull
    public Set<WarShip> findPooledShipsByUser(final int idUser) {
        return Objects.requireNonNullElse(warShipRepository.findPooledShipsByUser(idUser), new HashSet<>());
    }

    @Nonnull
    public Set<WarShip> findShipsByUser(final int idUser) {
        return Objects.requireNonNullElse(warShipRepository.findShipsByUser(idUser), new HashSet<>());
    }

    @Nonnull
    public List<WarShip> findAliveInoperationalForUser(final int idUser) {
        return Objects.requireNonNullElse(warShipRepository.findAliveInoperationalForUser(idUser), new ArrayList<>());
    }

    @Nonnull
    public List<WarShip> findAliveOperationalForUser(final int idUser) {
        return Objects.requireNonNullElse(warShipRepository.findAliveOperationalForUser(idUser), new ArrayList<>());
    }

    @Nonnull
    public List<WarShip> findAliveOperationalForPlanet(final int idPlanet) {
        return Objects.requireNonNullElse(warShipRepository.findAliveOperationalForPlanet(idPlanet), new ArrayList<>());
    }

    public void markAsInoperational(@Nonnull final Set<WarShip> ships) {
        Preconditions.checkNotNull(ships, "ships must not be empty");

        ships.forEach(s -> s.setOperational(false));
        saveAll(ships);
    }
}
