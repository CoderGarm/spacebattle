package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.yuga.spacebattle.backend.calculator.FittingUtils.DEFENSIVE_FITTING;
import static de.yuga.spacebattle.backend.calculator.FittingUtils.OFFENSIVE_FITTING;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.IMPELLER_WEDGE_PROTECTION;
import static de.yuga.spacebattle.backend.combat.round.CombatRound.COMBAT_ROUND;

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

    @Nonnull
    private Velocity velocity;

    @Nonnull
    private Direction direction;

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

    @Nonnull
    private CoursePlot coursePlot;

    @Nonnull
    private final AuraState auraState;

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
        this.fleetHealthState = new FleetHealthState(cage, fleet);
        this.coursePlot = new CoursePlot(cage, fleet, position);
        this.velocity = coursePlot.getAgentsVelocity();
        this.direction = coursePlot.getCurrentDirection();
        this.auraState = new AuraState(cage, fleet, this);
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
        this.coursePlot = state.getCoursePlot();
        this.velocity = coursePlot.getAgentsVelocity();
        this.direction = coursePlot.getCurrentDirection();
        this.auraState = new AuraState(cage, fleet, this);
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
        final CourseOrderElement courseElement = coursePlot.getCourseElement(cage.getCurrentCombatRound());
        if (courseElement != null) {
            return courseElement.getPosition();
        }
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

    @Nonnull
    public CoursePlot getCoursePlot() {
        return coursePlot;
    }

    @Nonnull
    public AuraState getAuraState() {
        return auraState;
    }

    public void setMovementType(@Nonnull final EMovementType movementType) {
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");

        this.movementType = movementType;
    }

    @Nonnull
    public Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(@Nonnull final Velocity velocity) {
        this.velocity = velocity;
    }

    @Nonnull
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(@Nonnull final Direction direction) {
        this.direction = direction;
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
     * Returns the possible distance which can be passed in one combat round towards a given direction.
     *
     * @param direction the direction
     * @return the distance which can be passed in the given direction
     */
    public Distance getMobilityForDirection(@Nonnull final Direction direction) {
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        return getMobilityForDirection(direction, 1);
    }

    /**
     * Returns the possible distance which can be passed in n combat round towards a given direction.
     *
     * @param direction            the direction
     * @param amountOfCombatRounds the amount of combat rounds -> n
     * @return the distance which can be passed in the given direction
     */
    public Distance getMobilityForDirection(@Nonnull final Direction direction, final int amountOfCombatRounds) {
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        final Direction agentsDirection = getDirection();
        final BigDecimal alignmentFactor = agentsDirection.getAlignmentFactor(direction);

        final Velocity velocity = getVelocity().getByAlignmentFactor(alignmentFactor);
        final Acceleration acceleration = getAccelerationFor(EModuleType.PROPULSION);
        return acceleration.getDistanceByTime(COMBAT_ROUND.multiply(amountOfCombatRounds), velocity, EDistanceMetric.LS);
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

    /**
     * Returns the damage which can be applied by this fleet over all ranges.
     *
     * @return the damage value
     */
    @Nonnull
    public List<DamagePerRangeAndAlignment> getDamagePerRangeAndAlignments() {
        return getFightingWarShips()
                .map(WarshipHealthState::getDamagePerRanges)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public FleetRoundState clone() {
        final FleetRoundState clone = (FleetRoundState) super.clone();
        clone.combatRound = combatRound.clone();
        clone.position = position.clone();
        clone.fleetHealthState = fleetHealthState.clone();
        clone.coursePlot = coursePlot.clone();
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
     * Checks if the fleet has any weapons for the given movement type.
     *
     * @param weaponType the weapon type which needs to have the correct alignment
     * @return <code>true</code> if there are any weapons which can fire, <code>false</code> otherwise
     */
    public boolean hasWeaponsForAlignment(@Nonnull final Set<EWeaponAlignment> applicableAlignments, @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(applicableAlignments, "applicableAlignments shouldn't be null!");
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
                        .anyMatch(f -> applicableAlignments.contains(f.getWeaponAlignment())));
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

    private boolean hasOffensiveWeaponry() {
        final List<AlignedFitting> alignedFittings = getFightingWarShips()
                .map(WarshipHealthState::getActiveFittings)
                .filter(fittings -> !fittings.isEmpty())
                .map(fittings -> fittings.stream().filter(OFFENSIVE_FITTING).findAny().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return !alignedFittings.isEmpty();
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
                    final ElectronicWarfare eloka = shipClass.getElectronicWarfare();
                    return eloka != null ? eloka.getEffectiveRange() : Distance.ZERO;
                }).max(Comparator.naturalOrder())
                .orElse(Distance.ZERO);
    }

    /**
     * Returns the range units which can be passed per turn based on the slowest ship.
     *
     * @return the maximal distance which could be passed in a tick
     */
    public Acceleration getAccelerationFor(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkArgument((eModuleType == EModuleType.FTLPROPULSION || eModuleType == EModuleType.PROPULSION),
                "EModuleType must be kind of propulsion.");

        final int lowestAcceleration = getLowestAccelerationValue();

        final EAccelerationMetric accelerationMetric = eModuleType == EModuleType.PROPULSION ? EAccelerationMetric.G : EAccelerationMetric.C;
        final EHyperBand activeBand = eModuleType == EModuleType.PROPULSION ? EHyperBand.NONE : getLowestHyperBand();
        return new Acceleration(BigDecimal.valueOf(lowestAcceleration), accelerationMetric, activeBand);
    }

    private int getLowestAccelerationValue() {
        return getFightingWarShips()
                .map(WarshipHealthState::getPropulsion)
                .map(Propulsion::getEffectValue)
                .min(Integer::compareTo)
                .orElse(0);
    }

    @Nonnull
    private EHyperBand getLowestHyperBand() {
        return getFightingWarShips()
                .map(WarshipHealthState::getPropulsion)
                .map(Propulsion::getHyperBand)
                .reduce((o1, o2) -> o1.getVelocityMultiplier() < o2.getVelocityMultiplier() ? o1 : o2)
                .orElse(EHyperBand.NONE);
    }

    @Nonnull
    public Velocity getMaxSubLightVelocity() {
        final ETechnologyType restrictingTechnologyType = getFightingWarShips()
                .map(WarshipHealthState::getPropulsion)
                .map(Propulsion::getTechnologyType)
                .reduce((o1, o2) -> o1.getMaxVelocitySOL() < o2.getMaxVelocitySOL() ? o1 : o2)
                .orElse(ETechnologyType.CIVIL);

        final BigDecimal vesselTopSpeed = EHyperBand.NONE.getEffectiveTopSpeed(restrictingTechnologyType);
        return new Velocity(vesselTopSpeed, EDistanceMetric.M, ETimeMetric.SECOND);
    }

    public boolean isAbleToAttack() {
        return hasOffensiveWeaponry();
    }


    @Nonnull
    public Set<Missile> getApplicableMissiles(@Nonnull final Set<EWeaponAlignment> applicableAlignments) {
        Preconditions.checkNotNull(applicableAlignments, "applicableAlignments must not be empty");

        return getFightingWarShips()
                .filter(WarshipHealthState::isFightingCapable)
                .flatMap(w -> w.getFittings().entrySet().stream()
                        // filter active fittings
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(a -> a.getWeaponType() == EWeaponType.MISSILE)
                        .filter(a -> a.getLauncher() != null)
                        .filter(f -> applicableAlignments.contains(f.getWeaponAlignment()))
                        .map(alignedFitting -> {
                            final Launcher launcher = alignedFitting.getLauncher();
                            final Set<Missile> allowedMissiles = launcher.getAllowedMissiles();
                            final HashSet<Missile> result = new HashSet<>(allowedMissiles);
                            for (final Missile allowedMissile : allowedMissiles) {
                                final MissileAmmunitionState missileAmmunitionState = w.getMissileAmmunitionState();
                                final int remainingShots = missileAmmunitionState.getRemainingShots(allowedMissile);
                                if (remainingShots <= 0) {
                                    result.remove(allowedMissile);
                                }
                            }
                            return result;
                        }))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
