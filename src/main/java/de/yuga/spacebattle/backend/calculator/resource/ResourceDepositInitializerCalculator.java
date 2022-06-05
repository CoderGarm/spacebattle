package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceDemand;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import java.util.Set;
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
     * @param techLevel the tech level defines the complexity of cost structure
     * @param clazz     the assigned class
     * @return the resource deposit
     */
    public static ResourceDeposit initializeResourceDeposit(@Nonnull final ETechLevel techLevel,
                                                            @Nonnull final EResourceDemand clazz) {
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        final EDepositType subType = EDepositType.COSTS;
        final Set<EResourceType> overrideResources = clazz.getOverrideResources();
        overrideResources.addAll(techLevel.getExcludedResources());

        final ResourceDeposit resourceDeposit = new ResourceDeposit(subType);
        for (final EResourceType type : EResourceType.valuesWithoutPopulation()) {
            if (overrideResources.contains(type)) {
                // do not generate for 'forbidden types'
                continue;
            }
            long rand = ThreadLocalRandom.current().nextLong(10, 51);
            resourceDeposit.setAbsoluteResourceValue(type, rand);
        }
        return resourceDeposit;
    }

    public static ResourceDeposit initializeResourceDeposit() {
        final ResourceDeposit resourceDeposit = new ResourceDeposit(EDepositType.DEPOSITS);
        for (final EResourceType type : EResourceType.valuesWithoutPopulation()) {
            // stay zero but to play games at start
            long rand = ThreadLocalRandom.current().nextLong(1000, 5100);
            resourceDeposit.setAbsoluteResourceValue(type, rand);
        }
        return resourceDeposit;
    }

    private static boolean isOrbitalConstruction(@Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        return clazz.isAssignableFrom(BaseModuleWithEffectValue.class)
                || clazz.isAssignableFrom(Hull.class)
                || clazz.isAssignableFrom(Missile.class)
                || clazz.isAssignableFrom(MissileMotor.class)
                || clazz.isAssignableFrom(Warhead.class)
                || clazz.isAssignableFrom(BaseModule.class);
    }
}
