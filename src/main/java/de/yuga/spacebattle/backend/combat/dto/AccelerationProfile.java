package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.DynamicInfo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;

import javax.annotation.Nonnull;

public class AccelerationProfile implements Comparable<AccelerationProfile> {

    @Nonnull
    private final CombatRound combatRound;

    @Nonnull
    private final DynamicInfo dynamicInfo;

    public AccelerationProfile(@Nonnull final CombatRound combatRound,
                               @Nonnull final Acceleration acceleration,
                               @Nonnull final Velocity velocity,
                               @Nonnull final Distance distance) {
        this.combatRound = Preconditions.checkNotNull(combatRound, "combatRound must not be empty").clone();
        this.dynamicInfo = new DynamicInfo(
                Preconditions.checkNotNull(acceleration, "acceleration must not be empty"),
                Preconditions.checkNotNull(velocity, "velocity must not be empty"),
                Preconditions.checkNotNull(distance, "distance must not be empty")
        );
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public DynamicInfo getDynamicInfo() {
        return dynamicInfo;
    }

    @Override
    public int compareTo(@Nonnull final AccelerationProfile o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        return combatRound.compareTo(o.combatRound);
    }

    @Override
    public String toString() {
        return combatRound + " " + dynamicInfo;
    }
}
