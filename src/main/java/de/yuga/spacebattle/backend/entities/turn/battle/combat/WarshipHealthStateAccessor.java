package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface WarshipHealthStateAccessor {

    double getStateByAsDouble(@Nonnull EModuleType eModuleType);

    int getStateByAsInt(@Nonnull EModuleType eModuleType);

    @Nonnull
    WarShip getWarShip();

    @Nonnull
    Set<AlignedFitting> getActiveFittings();

    @Nonnull
    Map<Missile, Integer> getRemainingShots();

    void setFightingCapable(boolean fightingCapable);

    boolean isFightingCapable();

    boolean isAlive();

    boolean isOperational();

    @Nonnull
    Set<CapabilityValue> getCapabilities();

    default boolean needsRepair() {
        if (!isFightingCapable() || !isAlive()) {
            return true;
        }

        final ShipClass shipClass = getWarShip().getShipClass();
        final Set<CapabilityValue> capabilityValues = new SpacecraftCalculator().getCapabilityValues(shipClass);

        return !(equalize10PointsOfDefaultStats(capabilityValues, EModuleType.ARMOR)
                && equalize10PointsOfDefaultStats(capabilityValues, EModuleType.ELECTRONIC_WARFARE)
                && equalize10PointsOfDefaultStats(capabilityValues, EModuleType.SIDEWALL)
                && equalize10PointsOfDefaultStats(capabilityValues, EModuleType.PROPULSION));
    }

    /**
     * If there is no module of a type the virtual default value is 10 instead of zero.<br>
     * This a workaround for the combat system which thinks zero points in something means destroyed.
     */
    private boolean equalize10PointsOfDefaultStats(@Nonnull final Set<CapabilityValue> capabilityValues, @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(capabilityValues, "capabilityValues must not be empty");
        Preconditions.checkNotNull(moduleType, "moduleType must not be empty");

        return Math.abs(getStateByAsInt(moduleType) - getInteger(capabilityValues, moduleType)) <= 10;
    }

    default boolean needsAmmunition() {
        if (!isFightingCapable() || !isAlive()) {
            return true;
        }

        final ShipClass shipClass = getWarShip().getShipClass();
        final Set<AmmunitionFitting> ammunitionFittings = shipClass.getAmmunitionFittings();
        final MissileAmmunitionState referenceMissiles = new MissileAmmunitionState(ammunitionFittings);
        return referenceMissiles.getRemainingShots().entrySet().stream().anyMatch(ref -> {
            final Missile missile = ref.getKey();
            final int refAmount = ref.getValue();
            final int remainingShots = getRemainingShots().getOrDefault(missile, 0);
            return refAmount != remainingShots;
        });
    }

    private int getInteger(@Nonnull final Set<CapabilityValue> capabilityValues, @Nonnull final EModuleType electronicWarfare) {
        Preconditions.checkNotNull(capabilityValues, "capabilityValues must not be empty");
        Preconditions.checkNotNull(electronicWarfare, "electronicWarfare must not be empty");

        return capabilityValues.stream().filter(c -> c.getModuleType() == electronicWarfare).findFirst().map(CapabilityValue::getValue).map(BigDecimal::intValue).orElse(0);
    }
}
