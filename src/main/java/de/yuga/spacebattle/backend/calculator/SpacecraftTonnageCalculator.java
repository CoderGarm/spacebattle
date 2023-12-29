package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SpacecraftTonnageCalculator {

    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

    /**
     * A single(!) hammerhead section takes in average "this" space or mass as part in the ratio to the main "cigar" hull section.
     */
    private static final BigDecimal HAMMERHEAD = java.math.BigDecimal.valueOf(10);

    /**
     * The main hull section takes in average "this" space or mass in the ratio so a single(!) hammerhead section.
     */
    private static final BigDecimal MAIN_HULL = BigDecimal.valueOf(44);

    /**
     * The average ratio between the cigar and a single hammerhead section.
     */
    private static final BigDecimal HULL_SECTION_RATIO = MAIN_HULL.divide(HAMMERHEAD, MC);


    private SpacecraftTonnageCalculator() {
    }

    /**
     * Returns the full tonnage of the class based on the physical requirements of the honorverse impeller physics.
     */
    @Nonnull
    public static Mass getFullTonnage(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        return shipClass.getTonnage(ECapacityAreaType.OVERALL);
    }

    @Nonnull
    private static BigDecimal getOrOne(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value must not be empty");

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return value;
    }

    /**
     * Returns the mass per section of the class.
     */
    @Nonnull
    public static Mass getTonnage(@Nonnull final ECapacityAreaType capacityAreaType, @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(capacityAreaType, "capacityAreaType must not be empty");
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        switch (capacityAreaType) {
            case BOW:
            case STERN:
            case BROADSIDE:
                assert capacityAreaType.getAlignment() != null : "Otherwise we had a problem.";
                return shipClass.getFittingByAlignment(capacityAreaType.getAlignment()).stream().map(AlignedFitting::getTonnage).reduce(Mass.ZERO, Mass::add);
            case MODULE:
                Mass tonnage = shipClass.getSupportFittings().stream().map(SupportFitting::getTonnage).reduce(Mass.ZERO, Mass::add);
                tonnage = tonnage.add(shipClass.getAmmunitionFittings().stream().map(AmmunitionFitting::getTonnage).reduce(Mass.ZERO, Mass::add));

                tonnage = tonnage.add(shipClass.getArmor() != null ? shipClass.getArmor().getTonnage() : Mass.ZERO);
                tonnage = tonnage.add(shipClass.getSidewall() != null ? shipClass.getSidewall().getTonnage() : Mass.ZERO);
                tonnage = tonnage.add(shipClass.getElectronicWarfare() != null ? shipClass.getElectronicWarfare().getTonnage() : Mass.ZERO);

                final Mass baseValue = tonnage.clone().add(getTonnage(ECapacityAreaType.BOW, shipClass)).add(getTonnage(ECapacityAreaType.STERN, shipClass)).add(getTonnage(ECapacityAreaType.BROADSIDE, shipClass));
                tonnage = tonnage.add(shipClass.getPropulsion().getTonnage(baseValue));
                return tonnage;
            case OVERALL:
                return ECapacityAreaType.getValuesWithoutOverall().stream().map(cap -> getTonnage(cap, shipClass)).reduce(Mass.ZERO, Mass::add);
            default:
                return Mass.ZERO;
        }
    }
}
