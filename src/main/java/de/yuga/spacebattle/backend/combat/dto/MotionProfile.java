package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;

public class MotionProfile implements Comparable<MotionProfile> {

    @Nonnull
    private final CombatRound combatRound;

    @Nonnull
    private final KinematicInfo kinematicInfo;

    public MotionProfile(@Nonnull final CombatRound combatRound,
                         @Nonnull final Acceleration acceleration,
                         @Nonnull final Velocity velocity,
                         @Nonnull final Direction direction,
                         @Nonnull final Orbit position) {
        this.combatRound = Preconditions.checkNotNull(combatRound, "combatRound must not be empty").clone();
        this.kinematicInfo = new KinematicInfo(
                Preconditions.checkNotNull(acceleration, "acceleration must not be empty"),
                Preconditions.checkNotNull(velocity, "velocity must not be empty"),
                Preconditions.checkNotNull(direction, "direction must not be empty"),
                Preconditions.checkNotNull(position, "position must not be empty")
        );
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public KinematicInfo getKinematicInfo() {
        return kinematicInfo;
    }

    @Override
    public int compareTo(@Nonnull final MotionProfile o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        return combatRound.compareTo(o.combatRound);
    }


    @Override
    public String toString() {
        return combatRound + " " + kinematicInfo;
    }
}
