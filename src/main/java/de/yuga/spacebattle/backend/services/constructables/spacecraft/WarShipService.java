package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.WarShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
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
    public List<WarShip> findByIds(List<Integer> ids) {
        final Iterable<WarShip> allById = warShipRepository.findAllById(ids);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    public WarShip find(@Nonnull final WarShip fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return warShipRepository.findById(fleet.getId()).orElse(null);
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

    public void delete(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        warShipRepository.delete(warShip);
    }

    public void deleteAll(@Nonnull final Collection<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        warShipRepository.deleteAll(warShips);
    }
}
