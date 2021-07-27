package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;

public class MovementAction extends Historizable<MovementAction> implements Cloneable {

    /**
     * The cage.
     */
    @Nonnull
    private final Cage cage;

    /**
     * The current combat round.<br>
     * A volley of direct weapons will hit in the same weapon.
     */
    @Nonnull
    private CombatRound combatRound;

    /**
     * The current phase.
     */
    @Nonnull
    private final ECombatPhase.ECombatSubPhase combatPhase = ECombatPhase.ECombatSubPhase.MOVEMENT_PHASE;

    /**
     * The source of the salvo.
     */
    @Nonnull
    private final Fleet actor;

    /**
     * The selected movement option for this action.
     */
    @Nonnull
    private final EMovementType movementType;

    /**
     * The starting position for this movement.
     */
    @Nonnull
    private Orbit origin;

    /**
     * The next step to the targeted position.
     */
    @Nonnull
    private Orbit destination;

    /**
     * The point of the targeted position.
     */
    @Nonnull
    private Orbit realDestination;

    public MovementAction(@Nonnull final Cage cage,
                          @Nonnull final Fleet actor,
                          @Nonnull final EMovementType movementType,
                          @Nonnull final Orbit origin,
                          @Nonnull final Orbit destination,
                          @Nonnull final Orbit realDestination) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkNotNull(realDestination, "realDestination shouldn't be null!");

        this.cage = cage;
        this.combatRound = cage.getCurrentCombatRound();
        this.actor = actor;
        this.movementType = movementType;
        this.origin = origin;
        this.destination = destination;
        this.realDestination = realDestination;
        historize();
    }

    private void historize() {
        //noinspection RedundantCast
        cage.addHistorizable((MovementAction) this);
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public EMovementType getMovementType() {
        return movementType;
    }

    @Nonnull
    public Orbit getOrigin() {
        return origin;
    }

    @Nonnull
    public Orbit getDestination() {
        return destination;
    }

    @Nonnull
    public Orbit getRealDestination() {
        return realDestination;
    }

    @Override
    public MovementAction clone() {
        final MovementAction clone = (MovementAction) super.clone();
        clone.combatRound = combatRound.clone();
        clone.origin = origin.clone();
        clone.destination = destination.clone();
        clone.realDestination = realDestination.clone();
        return clone;
    }
}
