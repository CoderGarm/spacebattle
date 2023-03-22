package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClassComparator;
import de.yuga.spacebattle.backend.repositories.spacecraft.ShipClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ShipClassService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassService.class);

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

        return shipClassRepository.saveAndFlush(entity);
    }

    /**
     * Marks a ship class as deleted.
     *
     * @param idShipClass the class to mark as deleted
     */
    public void delete(final int idUser, final int idShipClass) {
        final ShipClass shipClass = find(idShipClass);
        if (shipClass != null) {
            if (shipClass.getOwner().getId() == idUser) {
                delete(shipClass);
            } else {
                LOGGER.info("Deleting ship class not possible while user is not the owner idUser '{}' for idShipClass '{}'", idUser, idShipClass);
            }
        }
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
        entity.delete();
        toDelete.add(entity);
        ShipClass runner = entity;
        while (runner.getPredecessor() != null) {
            runner.delete();
            toDelete.add(runner);
            runner = runner.getPredecessor();
        }
        shipClassRepository.saveAll(toDelete);
    }

    @Nonnull
    public List<ShipClass> find(@Nonnull final Collection<Integer> idShipClasses) {
        Preconditions.checkNotNull(idShipClasses, "idShipClasses shouldn't be null!");

        final Iterable<ShipClass> allById = shipClassRepository.findAllById(idShipClasses);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Checks if the given class name is known for the user.<br>
     * The name must be between 3 and 30 characters.
     *
     * @param idOwner   the owner's id
     * @param className the class name to check
     * @return <code>true</code> if there are ship classes for this owner and name present, <code>false</code> otherwise
     */
    public boolean checkIfClassNameIsFree(final int idOwner, @Nonnull final String className) {
        Preconditions.checkNotNull(className, "className shouldn't be null!");

        return shipClassRepository.checkIfClassNameIsFree(idOwner, className);
    }

    public void saveAll(@Nonnull final Collection<ShipClass> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        shipClassRepository.saveAll(shipClasses);
    }
}
