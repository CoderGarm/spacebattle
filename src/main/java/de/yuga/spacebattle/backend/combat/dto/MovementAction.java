package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class MovementAction {

    @Nonnull
    private final Fleet actor;

    @Nonnull
    private final Maneuver maneuver;

    @Nonnull
    private final ManeuverElement maneuverElement;

    @Nonnull
    private final CombatRound combatRound;

    @Nonnull
    private final ECombatPhase.ECombatSubPhase combatPhase = ECombatPhase.ECombatSubPhase.MOVEMENT_PHASE;

    @Nonnull
    private final EMovementType movementType;

    @Nonnull
    private final Distance lengthOnTrack;

    @Nonnull
    private final Velocity velocity;

    @Nonnull
    private final Acceleration acceleration;

    public MovementAction(@Nonnull final Cage cage,
                          @Nonnull final Fleet actor,
                          @Nonnull final Maneuver maneuver,
                          @Nonnull final ManeuverElement maneuverElement,
                          @Nonnull final CourseOrderElement courseElement) {
        Preconditions.checkNotNull(courseElement, "courseElement must not be empty");
        this.actor = Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        this.maneuverElement = Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");
        this.movementType = courseElement.getMovementType();
        this.lengthOnTrack = courseElement.getLengthOnTrack();
        this.velocity = courseElement.getVelocity();
        this.acceleration = courseElement.getAcceleration();
        this.combatRound = cage.getCurrentCombatRound().clone();
    }

    public void historize() {
        maneuver.historizeMovementAction(this);
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public ECombatPhase.ECombatSubPhase getCombatPhase() {
        return combatPhase;
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public Maneuver getManeuver() {
        return maneuver;
    }

    @Nonnull
    public ManeuverElement getManeuverElement() {
        return maneuverElement;
    }

    @Nonnull
    public EMovementType getMovementType() {
        return movementType;
    }

    @Nonnull
    public Distance getLengthOnTrack() {
        return lengthOnTrack;
    }

    @Nonnull
    public Velocity getVelocity() {
        return velocity;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final MovementAction that = (MovementAction) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(maneuverElement, that.maneuverElement).append(combatRound, that.combatRound).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(maneuverElement).append(combatRound).toHashCode();
    }
}
