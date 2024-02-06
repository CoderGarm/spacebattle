package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class MovementAction extends Historizable<MovementAction> implements Cloneable {

    @Nonnull
    private final Cage cage;

    @Nonnull
    private final Fleet actor;

    @Nonnull
    private Maneuver maneuver;

    @Nonnull
    private ManeuverElement maneuverElement;

    @Nonnull
    private CombatRound combatRound;

    @Nonnull
    private final ECombatPhase.ECombatSubPhase combatPhase = ECombatPhase.ECombatSubPhase.MOVEMENT_PHASE;

    @Nonnull
    private final EMovementType movementType;

    @Nonnull
    private final Distance lengthOnTrack;

    @Nonnull
    private final Orbit position;

    public MovementAction(@Nonnull final Cage cage,
                          @Nonnull final Fleet actor,
                          @Nonnull final Maneuver maneuver,
                          @Nonnull final ManeuverElement maneuverElement,
                          @Nonnull final Distance lengthOnTrack,
                          @Nonnull final Orbit position,
                          @Nonnull final EMovementType movementType) {
        this.cage = Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        this.actor = Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        this.maneuverElement = Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");
        this.movementType = Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        this.lengthOnTrack = Preconditions.checkNotNull(lengthOnTrack, "lengthOnTrack must not be empty");
        this.position = Preconditions.checkNotNull(position, "position must not be empty");
        this.combatRound = cage.getCurrentCombatRound();
    }

    public void historize() {
        //noinspection RedundantCast
        cage.addHistorizable((MovementAction) this);
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
    public Orbit getPosition() {
        return position;
    }

    @Override
    public MovementAction clone() {
        final MovementAction clone = (MovementAction) super.clone();
        clone.combatRound = combatRound.clone();
        clone.maneuver = maneuver.clone();
        clone.maneuverElement = maneuverElement.clone();
        return clone;
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
