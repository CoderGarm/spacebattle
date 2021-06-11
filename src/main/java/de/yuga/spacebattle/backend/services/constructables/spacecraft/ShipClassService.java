package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClassComparator;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.ShipClassRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShipClassService {

    @Nonnull
    private final ShipClassRepository shipClassRepository;

    public ShipClassService(@Nonnull final ShipClassRepository shipClassRepository) {
        Preconditions.checkNotNull(shipClassRepository, "shipClassRepository shouldn't be null!");

        this.shipClassRepository = shipClassRepository;
    }

    /**
     * Will return an already sorted list of all ship classes.
     *
     * @return the sorted list of classes
     */
    @Nonnull
    public List<ShipClass> findAll() {
        return sort(shipClassRepository.findAllShipClasses());
    }

    /**
     * Will return an already sorted list of all ship classes for a given owner.
     *
     * @return the sorted list of classes
     */
    @Nonnull
    public List<ShipClass> findAllByOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        return sort(shipClassRepository.findAllShipClassesByOwner(owner));
    }

    /**
     * Will return an already sorted list of all the latest ship classes for a given owner.
     *
     * @return the sorted list of classes
     */
    @Nonnull
    public List<ShipClass> findAllLatestByOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        return sort(shipClassRepository.findAllLatestShipClassesByOwner(owner));
    }

    /**
     * Sorts a list of ship classes by {@link ShipClassComparator}.
     *
     * @param toSort the list to sort
     * @return the sorted list
     */
    @Nonnull
    private List<ShipClass> sort(@Nonnull final List<ShipClass> toSort) {
        Preconditions.checkNotNull(toSort, "toSort shouldn't be null!");

        toSort.sort(new ShipClassComparator());
        return toSort;
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

    /**
     * A {@link ShipClass} must be deleted with the deletion marker because it is necessary to leave the entities
     * existent to use deleted ship fittings in existing fleets.
     *
     * @param entity the entity to mark as deleted
     */
    public void delete(@Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        final Set<ShipClass> toDelete = new HashSet<>();
        ShipClass runner = entity;
        while (runner.getPredecessor() != null) {
            runner.setDeleted(true);
            toDelete.add(runner);
            runner = runner.getPredecessor();
        }
        shipClassRepository.saveAll(toDelete);
    }
}
