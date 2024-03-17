package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.BattleCalculator;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverFactory;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.BURST_ON_IMPELLER_WEDGE;
import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.DAMAGE_APPLIED;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.IMPELLER_WEDGE_PROTECTION;

/**
 * Represents a salvo of missiles.
 */
public class MissileSalvo extends DamageDealer {

    /**
     * The cage.
     */
    @Nonnull
    private final Cage cage;

    /**
     * The source of the salvo.
     */
    @Nonnull
    private final Fleet actor;

    /**
     * The target of the salvo.<br>
     * Currently, it cannot be resetted to set a new target.
     */
    @Nonnull
    private final Fleet target;

    @Nonnull
    private final CombatRound combatRound;

    /**
     * The composition of the salvo by missile type, amount and it's current state.
     */
    @Nonnull
    private final MissileSalvoHealthState missileSalvoHealthState;

    /**
     * If the salvo is inside detonation range and should not move.
     */
    private boolean isInDetonationRange = false;

    /**
     * The damage and the targets which were affected by the damage.
     */
    @Nonnull
    private final Map<WarShip, List<Long>> appliedDamage = new HashMap<>();

    /**
     * The result of this salvo.
     */
    @Nullable
    private EDamageResult result;

    @Nonnull
    private final Maneuver maneuver;

    public MissileSalvo(@Nonnull final Cage cage,
                        @Nonnull final Fleet actor,
                        @Nonnull final Fleet target,
                        @Nonnull final Set<EWeaponAlignment> applicableAlignments,
                        @Nonnull final Set<Missile> applicableMissiles) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(actor, "actor must not be empty");
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkNotNull(applicableAlignments, "applicableAlignments must not be empty");
        Preconditions.checkNotNull(applicableMissiles, "applicableMissiles must not be empty");

        this.cage = cage;

        this.actor = actor;
        this.target = target;
        this.combatRound = cage.getCurrentCombatRound().clone();
        final FleetRoundState actorsState = cage.getCurrentStateByFleet(this.actor);
        this.missileSalvoHealthState = new MissileSalvoHealthState(actorsState, applicableAlignments, applicableMissiles);
        this.maneuver = new ManeuverFactory(cage).createMissileTrail(this);
    }

    /**
     * Handles the {@link ECombatPhase#MISSILE_PHASE} for this particular salvo if it is active when reaching the next stage.
     */
    public void handleMissilePhase() {
        if (missileSalvoHealthState.isActive()) {
            handleElokaPhase();
        }
        if (missileSalvoHealthState.isActive()) {
            handleCounterMissilePhase();
        }
        if (missileSalvoHealthState.isActive()) {
            handleMovement();
        }
    }

    /**
     * This calculates and sets the range per round.<br>
     * Could be useful if the salvo is reduced to the slower missile types.
     */
    @Nonnull
    public Distance getRangePerCombatRound() {
        if (missileSalvoHealthState.isActive()) {
            final CombatRound currentCombatRound = cage.getCurrentCombatRound();
            final CombatRound combatRound = getCombatRound();
            final Velocity initialVelocity = maneuver.getAgentsKinematicInitial().getVelocity();
            // fixme modify the velocity about the direction of the salvo
            final int endurance = (currentCombatRound.getNo() - combatRound.getNo()) * CombatRound.COMBAT_ROUND_DURATION;
            return missileSalvoHealthState.getRangePerCombatRound(initialVelocity, endurance);
        }
        return Distance.ZERO.clone();
    }

    /**
     * Calculates the salvos attack range.
     */
    @Nonnull
    protected Distance getLongestOffensiveRange() {
        if (missileSalvoHealthState.isActive()) {
            return missileSalvoHealthState.getAttackRange();
        }
        return Distance.ZERO.clone();
    }

    /**
     * Handles the {@link ECombatSubPhase#ELOKA_PHASE} for this particular salvo.<br>
     * <p>
     * Handles the impact of the electronic counter measures of the target fleet against the missile salvo.
     */
    @VisibleForTesting
    protected void handleElokaPhase() {
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final Orbit targetsPosition = targetsState.getPosition();
        final Distance elokaRange = targetsState.getElokaRange();
        final Orbit position = getCurrentPosition();
        final Distance distance = position.getDistance(targetsPosition);
        if (elokaRange.compareTo(distance) <= 0) {
            // noop if eloka is not in range
            return;
        }


        final int elokaEffectValue = targetsState.getElokaEffectValue();
        final Map<Missile, Integer> lostByType = new HashMap<>();
        missileSalvoHealthState.getCurrentAmountByType().forEach((missile, amount) -> {
            final int elokaResistance = missile.getElokaResistance();
            int lostCounter = 0;
            for (int i = 1; i <= amount; i++) {
                final boolean isLost = BattleCalculator.calculateElokaImpact(elokaResistance, elokaEffectValue);
                if (isLost) {
                    // detect losses
                    lostCounter++;
                }
            }
            if (lostCounter > 0) {
                // memorize losses
                lostByType.put(missile, lostCounter);
            }
        });
        lostByType.forEach((missile, lostAmount) -> missileSalvoHealthState
                .addLostMissiles(ECombatSubPhase.ELOKA_PHASE, currentCombatRound, missile, lostAmount));

        cage.logMessage("Eloka attacked " + Integer.toHexString(hashCode())
                + " and killed " + lostByType.values().stream().mapToInt(Integer::intValue).sum()
                + " (" + missileSalvoHealthState.getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum()
                + " left) against " + target.getOwner().getUsername());
    }

    @Nonnull
    public Orbit getCurrentPosition() {
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final CourseOrderElement courseElement = maneuver.getCourseElement(currentCombatRound);
        return courseElement != null ? courseElement.getPosition() : maneuver.getAgentsKinematicInitial().getPosition();
    }

    /**
     * Handles the {@link ECombatSubPhase#COUNTER_MISSILE_PHASE}.<br>
     * Handles the impact of the counter missile weaponry of the target fleet against the missile salvo.<br>
     * <br>
     * Currently, all counter missile measures will be applied if the salvo is in range.<br>
     * The idea is that missiles are so fast that there is only one shot for every countermeasure.<br>
     * In principle, it makes no difference if the countermeasure is applied direct after the start or shortly before the impact.
     */
    @VisibleForTesting
    protected void handleCounterMissilePhase() {
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final Orbit targetsPosition = targetsState.getPosition();
        final Distance counterMissileRange = targetsState.getCounterMissileRange();
        final Distance distance = getCurrentPosition().getDistance(targetsPosition);
        if (counterMissileRange.compareTo(distance) <= 0) {
            // noop if counter missile is not in range
            return;
        }

        final Map<Missile, Integer> lostByType = new HashMap<>();
        final CounterMissileWeaponry counterMissileWeaponry = targetsState.getCounterMissileWeaponry();
        missileSalvoHealthState.getCurrentAmountByType().forEach((missile, amount) -> {
            final int lostCounter = counterMissileWeaponry.calculateDestroyedMissiles(missile, amount);
            if (lostCounter > 0) {
                // memorize losses
                lostByType.put(missile, lostCounter);
            }
        });
        lostByType.forEach((missile, lostAmount) -> missileSalvoHealthState
                .addLostMissiles(ECombatSubPhase.COUNTER_MISSILE_PHASE, currentCombatRound, missile, lostAmount));

        cage.logMessage("Counter attacked " + Integer.toHexString(hashCode()) + " and killed " + lostByType.values().stream().mapToInt(Integer::intValue).sum() + " (" + missileSalvoHealthState.getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum() + " left) against " + target.getOwner().getUsername());
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_MOVEMENT_PHASE}.<br>
     * Calculates and runs the new position for the movement of a combat round.
     */
    @VisibleForTesting
    protected void handleMovement() {
        if (isInDetonationRange) {
            // noop is already in detonation range
            return;
        }
        final FleetRoundState targetsCurrentStateByFleet = cage.getCurrentStateByFleet(target);
        if (targetsCurrentStateByFleet.getFleetHealthState().isNotFightingCapable()) {
            executeEffectiveDetonation();
            return;
        }

        final Orbit position = getCurrentPosition();
        final Distance distanceToTarget = position.getDistance(targetsCurrentStateByFleet.getPosition());

        if (distanceToTarget.compareTo(getLongestOffensiveRange()) <= 0) {
            isInDetonationRange = true;
            return;
        }

        final Distance minimalDistanceToAttack = distanceToTarget.subtract(getLongestOffensiveRange());
        if (minimalDistanceToAttack.compareTo(getRangePerCombatRound()) <= 0) {
            isInDetonationRange = true;
            return;
        }

        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final CourseOrderElement courseElement = maneuver.getCourseElement(currentCombatRound);
        if (courseElement == null) {
            // fixme remove workaround for missing plot by new course
            cage.logWarning("Salve '" + getUuid() + "' detonated without hit the target");
            executeEffectiveDetonation();
            return;
        }

        // the last movement will probably not be placed at the fleets position - will be set on detonation
        executeLatestPendingOrder();
    }

    public void executeLatestPendingOrder() {
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final CourseOrderElement courseElement = maneuver.getCourseElement(currentCombatRound);
        Preconditions.checkState(courseElement != null, "courseElement shouldn't be null!");
        courseElement.executeOrder();
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_FIRE_INCOMING_PHASE} for this particular salvo.<br>
     * Handles the damage application to the target fleet.
     */
    public void detonate() {
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        if (IMPELLER_WEDGE_PROTECTION == targetsState.getMovementType()) {
            result = BURST_ON_IMPELLER_WEDGE;
        } else {
            final FleetHealthState targetHealthState = targetsState.getFleetHealthState();
            missileSalvoHealthState.getCurrentAmountByType().forEach((missile, missileAmount) -> {
                final long applicableDamage = missile.getWarhead().getDamageValue();
                for (int i = 1; i <= missileAmount; i++) {
                    final WarShip targetedWarShip = cage.getRandomActiveWarShipOfFleet(target);
                    if (targetedWarShip == null) {
                        // noop - no targets left
                        return;
                    }
                    targetHealthState.applyDamage(targetedWarShip, applicableDamage, this).ifPresent(warShip -> {
                        final List<Long> alreadyAppliedDamages = appliedDamage.computeIfAbsent(warShip, k -> new ArrayList<>());
                        alreadyAppliedDamages.add(applicableDamage);
                        appliedDamage.put(warShip, alreadyAppliedDamages);
                    });
                }
            });
            result = DAMAGE_APPLIED;
        }
        executeEffectiveDetonation();
    }

    public void executeEffectiveDetonation() {
        missileSalvoHealthState.getCurrentAmountByType().clear();
        maneuver.setEnd(cage.getCurrentCombatRound());
    }

    public boolean isInDetonationRange() {
        return isInDetonationRange;
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    public boolean isActive() {
        return missileSalvoHealthState.isActive();
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public Fleet getTarget() {
        return target;
    }

    @Nonnull
    public MissileSalvoHealthState getMissileSalvoHealthState() {
        return missileSalvoHealthState;
    }

    @Nonnull
    public Map<WarShip, List<Long>> getAppliedDamage() {
        return appliedDamage;
    }

    @Nullable
    public EDamageResult getResult() {
        return result;
    }

    @Nonnull
    public Maneuver getManeuver() {
        return maneuver;
    }

    /**
     * Returns the possible damage which can be applied by this volley.
     *
     * @return the damage potential
     */
    @Nonnull
    public List<ApplicableDamage> getApplicableDamage() {
        return missileSalvoHealthState.getCurrentAmountByType().entrySet().stream().map(e -> {
            final List<ApplicableDamage> result = new ArrayList<>();
            for (int i = 1; i <= e.getValue(); i++) {
                result.add(new ApplicableDamage(e.getKey()));
            }
            return result;
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return missileSalvoHealthState.getCurrentAmountByType().entrySet().stream().filter(e -> e.getValue() > 0)
                .reduce((o1, o2) -> {
                    final Missile m1 = o1.getKey();
                    final Missile m2 = o2.getKey();

                    // return smallest acceleration
                    return m1.getMissileMotor().getAcceleration().compareTo(m2.getMissileMotor().getAcceleration()) < 0 ? o2 : o1;
                })
                .map(Map.Entry::getKey)
                .map(Missile::getMissileMotor)
                .map(MissileMotor::getAcceleration)
                .orElseThrow(() -> new NotifyWebUserException("Please ask me not how fast I am when I don't contain missiles."));
    }
}
