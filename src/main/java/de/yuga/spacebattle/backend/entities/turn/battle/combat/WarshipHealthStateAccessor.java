package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
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

    @Nonnull
    Set<CapabilityValue> getCapabilities();

    default boolean hasChanged() {
        if (!isFightingCapable() || !isAlive()) {
            return true;
        }

        final ShipClass shipClass = getWarShip().getShipClass();
        final Set<CapabilityValue> capabilityValues = new SpacecraftCalculator().getCapabilityValues(shipClass);

        final boolean differState = !(getStateByAsInt(EModuleType.ARMOR) == getInteger(capabilityValues, EModuleType.ARMOR)
                && getStateByAsInt(EModuleType.ELECTRONIC_WARFARE) == getInteger(capabilityValues, EModuleType.ELECTRONIC_WARFARE)
                && getStateByAsInt(EModuleType.SIDEWALL) == getInteger(capabilityValues, EModuleType.SIDEWALL)
                && getStateByAsInt(EModuleType.PROPULSION) == getInteger(capabilityValues, EModuleType.PROPULSION));

        final Set<AmmunitionFitting> ammunitionFittings = shipClass.getAmmunitionFittings();

        final MissileAmmunitionState referenceMissiles = new MissileAmmunitionState(ammunitionFittings);
        final boolean differMissiles = referenceMissiles.getRemainingShots().entrySet().stream().anyMatch(ref -> {
            final Missile missile = ref.getKey();
            final int refAmount = ref.getValue();
            final int remainingShots = getRemainingShots().get(missile);
            return refAmount != remainingShots;
        });

        return differState || differMissiles;
    }

    private int getInteger(final Set<CapabilityValue> capabilityValues, final EModuleType electronicWarfare) {
        return capabilityValues.stream().filter(c -> c.getModuleType() == electronicWarfare).findFirst().map(CapabilityValue::getValue).map(BigDecimal::intValue).orElse(0);
    }
}
