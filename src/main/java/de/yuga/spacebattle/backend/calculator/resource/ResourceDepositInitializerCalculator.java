package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Should create resource deposits - think before live because of balancing issues.
 */
public class ResourceDepositInitializerCalculator {

    private ResourceDepositInitializerCalculator() {
    }

    /**
     * Creates costs and deposits.<br>
     * Needs a heavy revision for productive run. Must be balanced.
     *
     * @param techLevel the tech level defines the complexity of cost structure
     * @param capacity  if present it will be taken to weight the costs against other modules
     * @param clazz     the assigned class
     * @return the resource deposit
     */
    public static ResourceDeposit initializeCosts(@Nonnull final ETechLevel techLevel,
                                                  @Nullable final Integer capacity,
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
            final int origin = getOrigin(capacity, clazz);
            final int bound = getBound(capacity, clazz);
            int next = new Random().nextInt(bound);
            resourceDeposit.setAbsoluteResourceValue(type, origin + next);
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

    public static ResourceDeposit getInfiniteDeposit() {
        final ResourceDeposit resourceDeposit = new ResourceDeposit(EDepositType.DEPOSITS);
        for (final EResourceType type : EResourceType.valuesWithoutPopulation()) {
            resourceDeposit.setAbsoluteResourceValue(type, Long.MAX_VALUE);
        }
        for (final EEducationType type : EEducationType.values()) {
            resourceDeposit.setAbsoluteCrewRequirement(type, Long.MAX_VALUE);
        }
        return resourceDeposit;
    }

    private static int getOrigin(@Nullable Integer capacity, @Nonnull final EResourceDemand clazz) {
        Preconditions.checkNotNull(clazz, "clazz must not be empty");

        switch (clazz) {
            case BUILDING:
            case RESEARCH:
            default:
                return 10;
            case HULL:
            case MISSILE:
            case WARHEAD:
            case MISSILE_MOTOR:
            case BASE_MODULE:
                Preconditions.checkNotNull(capacity, "capacity must not be empty");
                return Integer.max(4, capacity / 10);
        }
    }

    private static int getBound(@Nullable Integer capacity, @Nonnull final EResourceDemand clazz) {
        Preconditions.checkNotNull(clazz, "clazz must not be empty");

        switch (clazz) {
            case BUILDING:
            case RESEARCH:
            default:
                return 51;
            case HULL:
            case MISSILE:
            case WARHEAD:
            case MISSILE_MOTOR:
            case BASE_MODULE:
                Preconditions.checkNotNull(capacity, "capacity must not be empty");
                return Integer.max(9, capacity / 2);
        }
    }
}
