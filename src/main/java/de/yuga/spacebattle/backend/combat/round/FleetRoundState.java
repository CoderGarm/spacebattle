package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.CounterMissileWeaponry;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.yuga.spacebattle.backend.calculator.FittingUtils.DEFENSIVE_FITTING;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.IMPELLER_WEDGE_PROTECTION;

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

    /**
     * The movement type which is currently active.
     */
    @Nullable
    private EMovementType movementType;

    public FleetRoundState(@Nonnull final Cage cage,
                           @Nonnull final Fleet fleet,
                           @Nonnull final Orbit position) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(position, "position shouldn't be null!");

        this.cage = cage;
        this.combatRound = cage.getCurrentCombatRound();
        this.fleet = fleet;
        this.position = position.clone();
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
        this.movementType = state.getMovementType();
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

    @Nullable
    public EMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(@Nonnull final EMovementType movementType) {
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");

        this.movementType = movementType;
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


    /**
     * Returns the maximum weapon range of the fleet.
     *
     * @return the maximum weapon range
     */
    @Nonnull
    public Distance getMaximumWeaponRange() {
        return getFightingWarShips().map(WarshipHealthState::getMaximumWeaponRange).max(Distance::compareTo).orElse(Distance.ZERO);
    }

    /**
     * Returns the maximum weapon range of the fleet.
     *
     * @param weaponType the weapon type as filter
     * @return the maximum weapon range
     */
    @Nonnull
    public Distance getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return getFightingWarShips().map(w -> w.getMaximumWeaponRangePerType(weaponType)).max(Distance::compareTo).orElse(Distance.ZERO);
    }

    /**
     * Returns the damage which can be applied by this fleet to the given range in meter.
     *
     * @param boundaries the boundaries
     * @return the damage value
     */
    @Nonnull
    public List<DamagePerRangeAndAlignment> getDamagePerRange(@Nonnull final RangeDefinition boundaries) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");

        return getFightingWarShips()
                .map(warshipHealthState -> warshipHealthState.getDamagePerRange(boundaries))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public FleetRoundState clone() {
        final FleetRoundState clone = (FleetRoundState) super.clone();
        clone.combatRound = combatRound.clone();
        clone.position = position.clone();
        clone.fleetHealthState = fleetHealthState.clone();
        return clone;
    }

    /**
     * Checks if the fleet has any weapons for the given movement type.
     *
     * @param weaponType the weapon type which needs to have the correct alignment
     * @return <code>true</code> if there are any weapons which can fire, <code>false</code> otherwise
     */
    public boolean hasWeaponsForAlignment(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        if (IMPELLER_WEDGE_PROTECTION == movementType) {
            return false;
        }
        return getFightingWarShips()
                .anyMatch(w -> w.getFittings().entrySet().stream()
                        // filter active fittings
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(a -> a.getWeaponType() == weaponType)
                        .anyMatch(f -> f.getWeaponAlignment().isAssignableFromMovementType(movementType)));
    }

    /**
     * Returns the range of the fleets counter missile weaponry.
     *
     * @return the range
     */
    @Nonnull
    public Distance getCounterMissileRange() {
        return getFightingWarShips()
                .map(WarshipHealthState::getCounterMissileRange)
                .max(Comparator.naturalOrder())
                .orElse(Distance.ZERO);
    }

    /**
     * Returns the effect value of the fleets electronic countermeasures.
     *
     * @return the eloka range
     */
    public int getElokaEffectValue() {
        return getFightingWarShips()
                .map(WarshipHealthState::getElokaState)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Nonnull
    public Stream<WarshipHealthState> getFightingWarShips() {
        return fleetHealthState.getWarshipHealthStates().values().stream()
                .filter(WarshipHealthState::isFightingCapable);
    }

    /**
     * Returns all anti missile weapons of this fleet.
     *
     * @return the anti missile weapons
     */
    @Nonnull
    public CounterMissileWeaponry getCounterMissileWeaponry() {
        final List<AlignedFitting> alignedFittings = getFightingWarShips()
                .map(WarshipHealthState::getActiveFittings)
                .filter(fittings -> !fittings.isEmpty())
                .map(fittings -> fittings.stream().filter(DEFENSIVE_FITTING).findAny().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new CounterMissileWeaponry(alignedFittings);
    }

    /**
     * Returns the range of the fleets electronic countermeasures.
     *
     * @return the eloka range
     */
    @Nonnull
    public Distance getElokaRange() {
        return getFightingWarShips()
                .map(shipClass -> {
                    final ElectronicWarfare eloka = shipClass.getModule(ElectronicWarfare.class);
                    return eloka != null ? eloka.getEffectiveRange() : Distance.ZERO;
                }).max(Comparator.naturalOrder())
                .orElse(Distance.ZERO);
    }
}
