package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SpacecraftTonnageCalculator {

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

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

        final Mass bowMass = shipClass.getTonnage(ECapacityAreaType.BOW);
        final Mass sternMass = shipClass.getTonnage(ECapacityAreaType.STERN);

        final Mass max = bowMass.max(sternMass);
        final Mass broadsidesMass = shipClass.getTonnage(ECapacityAreaType.BROADSIDE).add(shipClass.getTonnage(ECapacityAreaType.MODULE));

        final BigDecimal hammerhead = getOrOne(max.getCoordinateInMetric(EMassMetric.KT));
        final BigDecimal cigar = getOrOne(broadsidesMass.getCoordinateInMetric(EMassMetric.KT));

        final BigDecimal realRatio = cigar.divide(hammerhead, MC);
        final int compareTo = realRatio.compareTo(HULL_SECTION_RATIO);

        Mass mass;
        if (compareTo < 0) {
            // hammerhead is heavier than cigar
            mass = max.add(max);
            final Mass newBroadsideMass = max.multiply(HULL_SECTION_RATIO);
            mass = mass.add(newBroadsideMass);
        } else if (compareTo > 0) {
            // cigar is heavier than hammerhead
            mass = broadsidesMass;
            final Mass newHammerheadMass = broadsidesMass.divide(HULL_SECTION_RATIO);
            mass = mass.add(newHammerheadMass).add(newHammerheadMass);
        } else {
            // perfect fit
            mass = shipClass.getTonnage();
        }
        return mass;
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
