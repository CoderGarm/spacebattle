package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.ShipClassRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class ShipClassService {

    @Nonnull
    private final ShipClassRepository shipClassRepository;

    public ShipClassService(@Nonnull final ShipClassRepository shipClassRepository) {
        Preconditions.checkNotNull(shipClassRepository, "shipClassRepository shouldn't be null!");

        this.shipClassRepository = shipClassRepository;
    }

    @Nonnull
    public List<ShipClass> findAll() {
        return shipClassRepository.findAllShipClasses();
    }

    @Nonnull
    public List<ShipClass> findAllByOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        return shipClassRepository.findAllShipClassesByOwner(owner);
    }

    @Nullable
    public ShipClass find(@Nonnull final Integer idShipClass) {
        Preconditions.checkNotNull(idShipClass, "idShipClass shouldn't be null!");

        return shipClassRepository.findById(idShipClass).orElse(null);
    }

    @Nullable
    public ShipClass find(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        return find(shipClass.getId());
    }

    /**
     * Creates a new {@link ShipClass}.
     *
     * @param name  the name of the planet
     * @param owner the guy who colonized this planet - or even not
     * @param hull  the hull type
     * @return the new planet
     */
    @Nonnull
    public ShipClass createShipClass(@Nonnull final User owner,
                                     @Nonnull final String name,
                                     @Nonnull final Hull hull) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        return shipClassRepository.save(new ShipClass(owner, name, hull));
    }

    @Nonnull
    public ShipClass save(@Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return shipClassRepository.save(entity);
    }

    @Nonnull
    public ShipClass saveAndFlush(@Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return shipClassRepository.saveAndFlush(entity);
    }

    public void delete(@Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        shipClassRepository.delete(entity);
    }
}
