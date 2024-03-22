package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;

public class CourseOrderElement implements Cloneable {

    @Nonnull
    private Maneuver maneuver;

    @Nonnull
    private final ManeuverElement maneuverElement;

    @Nonnull
    private final Distance lengthOnTrack;

    @Nonnull
    private CombatRound combatRound;

    @Nonnull
    private Velocity velocity;

    @Nonnull
    private final Acceleration acceleration;

    @Nonnull
    private Orbit position;

    @Nonnull
    private final EMovementType movementType;

    private boolean courseOrderExecuted = false;

    public CourseOrderElement(@Nonnull final Maneuver maneuver,
                              @Nonnull final ManeuverElement maneuverElement,
                              @Nonnull final Distance lengthOnTrack,
                              @Nonnull final CombatRound combatRound,
                              @Nonnull final EMovementType movementType,
                              @Nonnull final Velocity velocity,
                              @Nonnull final Acceleration acceleration,
                              @Nonnull final Orbit position) {
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        this.maneuverElement = Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");
        this.lengthOnTrack = Preconditions.checkNotNull(lengthOnTrack, "lengthOnTrack must not be empty");
        this.movementType = Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        this.combatRound = Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        this.velocity = Preconditions.checkNotNull(velocity, "velocity shouldn't be null!");
        this.acceleration = Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        this.position = Preconditions.checkNotNull(position, "position shouldn't be null!");
    }

    public void executeOrder() {
        this.courseOrderExecuted = true;
    }

    @Nonnull
    public Maneuver getManeuver() {
        return maneuver;
    }

    public void setManeuver(@Nonnull final Maneuver maneuver) {
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
    }

    @Nonnull
    public ManeuverElement getManeuverElement() {
        return maneuverElement;
    }

    @Nonnull
    public Distance getLengthOnTrack() {
        return lengthOnTrack;
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public Velocity getVelocity() {
        return velocity;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }

    @Nonnull
    public Orbit getPosition() {
        return position;
    }

    @Nonnull
    public EMovementType getMovementType() {
        return movementType;
    }

    public boolean isCourseOrderExecuted() {
        return courseOrderExecuted;
    }

    @Override
    public CourseOrderElement clone() {
        try {
            final CourseOrderElement clone = (CourseOrderElement) super.clone();
            clone.combatRound = combatRound.clone();
            clone.velocity = velocity.clone();
            clone.position = position.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
