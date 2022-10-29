package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
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
}
