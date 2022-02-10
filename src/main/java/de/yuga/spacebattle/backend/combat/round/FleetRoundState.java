package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void setMovementType(@Nullable final EMovementType movementType) {
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
    public BigDecimal getMaximumWeaponRange() {
        final List<WarshipHealthState> warshipHealthStatesByWeaponRange = fleetHealthState.getWarshipHealthStates().values().stream()
                .sorted((o1, o2) -> {
                    final BigDecimal maximumWeaponRangeO1 = o1.getMaximumWeaponRange();
                    final BigDecimal maximumWeaponRangeO2 = o2.getMaximumWeaponRange();
                    return maximumWeaponRangeO1.compareTo(maximumWeaponRangeO2);
                }).collect(Collectors.toList());
        return warshipHealthStatesByWeaponRange.get(warshipHealthStatesByWeaponRange.size() - 1).getMaximumWeaponRange();
    }

    /**
     * Returns the maximum weapon range of the fleet.
     *
     * @param weaponType the weapon type as filter
     * @return the maximum weapon range
     */
    public BigDecimal getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        final List<WarshipHealthState> warshipHealthStatesByWeaponRange = fleetHealthState.getWarshipHealthStates().values().stream()
                .sorted((o1, o2) -> {
                    final BigDecimal maximumWeaponRangeO1 = o1.getMaximumWeaponRangePerType(weaponType);
                    final BigDecimal maximumWeaponRangeO2 = o2.getMaximumWeaponRangePerType(weaponType);
                    return maximumWeaponRangeO1.compareTo(maximumWeaponRangeO2);
                }).collect(Collectors.toList());
        return warshipHealthStatesByWeaponRange.get(warshipHealthStatesByWeaponRange.size() - 1).getMaximumWeaponRangePerType(weaponType);
    }

    /**
     * Returns the damage which can be applied by this fleet to the given range in meter.
     *
     * @param lowerBound the lower boundary
     * @param upperBound the upper boundary
     * @return the damage value
     */
    public List<DamagePerRangeAndAlignment> getDamagePerRange(@Nonnull final BigDecimal lowerBound, @Nonnull final BigDecimal upperBound) {
        Preconditions.checkNotNull(lowerBound, "lowerBound shouldn't be null!");
        Preconditions.checkNotNull(upperBound, "upperBound shouldn't be null!");

        return fleetHealthState.getWarshipHealthStates().values().stream()
                .map(warshipHealthState -> warshipHealthState.getDamagePerRange(lowerBound, upperBound))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public long getMaximumDamage() {
        return fleetHealthState.getWarshipHealthStates().values().stream()
                .map(WarshipHealthState::getMaximumDamage)
                .mapToLong(Long::longValue).sum();
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
        return fleetHealthState.getWarshipHealthStates().values().stream()
                .filter(WarshipHealthState::isFightingCapable)
                .anyMatch(w -> w.getFittings().entrySet().stream()
                        // filter active fittings
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(a -> a.getWeaponType() == weaponType)
                        .anyMatch(f -> f.getWeaponAlignment().isAssignableFromMovementType(movementType)));
    }
}
