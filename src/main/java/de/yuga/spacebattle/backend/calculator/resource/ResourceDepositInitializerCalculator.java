package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Should create resource deposits - think before live because of balancing issues.
 */
public class ResourceDepositInitializerCalculator {

    private ResourceDepositInitializerCalculator() {
    }

    /**
     * Should create costs and deposits.<br>
     * Needs a heavy revision for productive run. Must be balanced.
     *
     * @param clazz   the assigned class
     * @param subType the sub-type
     * @return the resource deposit
     */
    public static ResourceDeposit initializeResourceDeposit(@Nonnull final Class<?> clazz,
                                                            @Nonnull final EDepositType subType) {
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        final List<EResourceType> overrideResources = new ArrayList<>();
        if (clazz.isAssignableFrom(Planet.class) || clazz.isAssignableFrom(Colonization.class)) {
            // continue - just for the sake of completeness
            if (EDepositType.DEPOSITS != subType) {
                throw new NotifyUserException("Initialization of deposit not possible for '" + subType + "'.");
            }
        } else if (clazz.isAssignableFrom(Building.class) || clazz.isAssignableFrom(Research.class)) {
            overrideResources.add(EResourceType.ORBITAL_CONSTRUCTION);
            overrideResources.add(EResourceType.CONSTRUCTION);
        } else if (clazz.isAssignableFrom(BaseModuleWithEffectValue.class) || clazz.isAssignableFrom(Hull.class) ||
                clazz.isAssignableFrom(Missile.class) || clazz.isAssignableFrom(MissileMotor.class) || clazz.isAssignableFrom(Warhead.class)) {
            overrideResources.add(EResourceType.CONSTRUCTION);
            overrideResources.add(EResourceType.RESEARCH);
        } else {
            throw new NotifyUserException("Initialization of resources not possible for class '" + clazz.getName() + "'.");
        }

        final ResourceDeposit resourceDeposit = new ResourceDeposit(subType);
        for (final EResourceType type : EResourceType.valuesWithoutPopulation()) {
            if (overrideResources.contains(type)) {
                // do not generate for 'forbidden types'
                continue;
            }
            long rand = 0;
            switch (subType) {
                case COSTS:
                    rand = ThreadLocalRandom.current().nextLong(10, 51);
                    break;
                case DEPOSITS:
                    // stay zero but to play games at start
                    rand = ThreadLocalRandom.current().nextLong(1000, 5100);
                    break;
            }
            resourceDeposit.setAbsoluteResourceValue(type, rand);
        }
        return resourceDeposit;
    }
}
