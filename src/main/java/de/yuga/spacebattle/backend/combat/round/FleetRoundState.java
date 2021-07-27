package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;

public class FleetRoundState extends Historizable<FleetRoundState> implements Cloneable {

    /**
     * The cage.
     */
    @Nonnull
    private final Cage cage;

    /**
     * The combat round of this state.
     */
    @Nonnull
    private CombatRound combatRound;

    /**
     * The acting fleet.
     */
    @Nonnull
    private final Fleet fleet;

    /**
     * The current position of the acting fleet.
     */
    @Nonnull
    private Orbit position;

    /**
     * The health state for the fleet of this round.
     */
    @Nonnull
    private FleetHealthState fleetHealthState;

    /**
     * Initiative for {@link #fleet}.<br>
     * The lower the initiative is, the earlier has the actor to move. It is better to move later in order to react on the earlier movement of other fleets.
     */
    private int movementInitiative;

    public FleetRoundState(@Nonnull final Cage cage,
                           @Nonnull final Fleet fleet,
                           @Nonnull final Orbit position) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(position, "position shouldn't be null!");

        this.cage = cage;
        this.combatRound = cage.getCurrentCombatRound();
        this.fleet = fleet;
        this.position = position;
        this.fleetHealthState = new FleetHealthState(fleet);
        historize();
    }

    public FleetRoundState(@Nonnull final Cage cage,
                           @Nonnull final FleetRoundState state) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(state, "state shouldn't be null!");

        this.cage = cage;
        this.combatRound = cage.getCurrentCombatRound();
        this.fleet = state.getFleet();
        this.position = state.getPosition().clone();
        this.fleetHealthState = state.getFleetHealthState();
        historize();
    }

    /**
     * Determines the initiative for the actors.<br>
     * The lower the initiative is, the earlier has the actor to move. It is better to move later in order to react on the earlier movement of other fleets.
     */
    public void determineMovementInitiative() {
        movementInitiative = (int) ((Math.random() * 10) + 0);
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    public int getMovementInitiative() {
        return movementInitiative;
    }

    @Nonnull
    public Orbit getPosition() {
        return position;
    }

    @Nonnull
    public FleetHealthState getFleetHealthState() {
        return fleetHealthState;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    /**
     * Checks if this is matching the given parameters.
     *
     * @param combatRound the round
     * @param fleet       the fleet
     * @return <code>true</code> if the parameters are matching, <code>false</code> otherwise
     */
    public boolean isEqualsByFleetAndRound(@Nonnull final CombatRound combatRound, @Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return this.combatRound.equals(combatRound) && this.fleet.equals(fleet);
    }

    public void historize() {
        //noinspection RedundantCast
        cage.addHistorizable((FleetRoundState) this);
    }

    @Override
    public FleetRoundState clone() {
        final FleetRoundState clone = (FleetRoundState) super.clone();
        clone.combatRound = combatRound.clone();
        clone.position = position.clone();
        clone.fleetHealthState = fleetHealthState.clone();
        return clone;
    }
}
