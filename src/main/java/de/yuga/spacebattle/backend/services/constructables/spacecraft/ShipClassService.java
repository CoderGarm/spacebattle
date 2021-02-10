package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.ShipClassRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Nullable
    public ShipClass find(@Nonnull final Integer idShipClass) {
        Preconditions.checkNotNull(idShipClass, "idShipClass shouldn't be null!");
        return shipClassRepository.findById(idShipClass).orElse(null);
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

    /**
     * Adds {@link Module}s to this {@link ShipClass}.
     *
     * @param shipClass the ship class to modify
     * @param modules   the modules to add
     * @return the modified class
     */
    public ShipClass addModules(@Nonnull ShipClass shipClass,
                                @Nonnull final Module... modules) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        shipClass = shipClassRepository.findById(shipClass.getId()).orElse(null);
        if (shipClass == null) {
            throw new NotifySBUserException("There is no class to modify.");
        }
        int capacity = shipClass.getHull().getConstructionCapacity();
        Map<Module, Integer> alreadyUsedModules = shipClass.getModules();
        int usedCapacity = alreadyUsedModules.keySet().stream().map(module -> {
            Integer amount = alreadyUsedModules.get(module);
            int useCapacity = module.getUseCapacity();
            return amount * useCapacity;
        }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();

        int neededCapacity = Arrays.stream(modules).map(module -> {
            Integer amount = alreadyUsedModules.getOrDefault(module, 0);
            int useCapacity = module.getUseCapacity();
            return amount * useCapacity;
        }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();

        if (capacity - usedCapacity - neededCapacity < 0) {
            throw new NotifySBUserException("There is not enough free construction capacity in this class.");
        }

        for (final Module m : modules) {
            shipClass.addModule(m);
        }
        return shipClassRepository.save(shipClass);
    }

    public ShipClass save(@Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return shipClassRepository.save(entity);
    }
}
