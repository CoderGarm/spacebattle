package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.BattleCalculator;
import de.yuga.spacebattle.backend.combat.BattleStaticLogger;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a salvo of missiles.
 */
public class MissileSalvo extends Historizable<MissileSalvo> implements Cloneable {

    /**
     * The cage.
     */
    @Nonnull
    private final Cage cage;

    /**
     * The current combat round.
     */
    @Nonnull
    private CombatRound combatRound;

    /**
     * The current phase.
     */
    @Nonnull
    private ECombatSubPhase combatSubPhase;

    /**
     * The current position of the salvo.
     */
    @Nonnull
    private Orbit position;

    /**
     * The position of the salvo in the last round.
     */
    @Nonnull
    private Orbit lastPosition;

    /**
     * The current position of the target.
     */
    @Nonnull
    private Orbit targetPosition;

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

    /**
     * The initial distance of this shot.
     */
    private final BigDecimal initialDistance;

    /**
     * The composition of the salvo by missile type, amount and it's current state.
     */
    @Nonnull
    private MissileSalvoHealthState missileSalvoHealthState;

    /**
     * The distance which can be covered per combat round.
     */
    @Nonnull
    private BigDecimal rangePerCombatRound = BigDecimal.ZERO;

    /**
     * The distance in which the damage can be applied to the target.
     */
    @Nonnull
    private BigDecimal longestOffensiveRange = BigDecimal.ZERO;

    /**
     * If the salvo is inside detonation range and should not move.
     */
    private boolean isInDetonationRange = false;

    /**
     * The damage and the targets which were affected by the damage.
     */
    @Nonnull
    private final Map<WarShip, List<Long>> appliedDamage = new HashMap<>();

    public MissileSalvo(@Nonnull final Cage cage,
                        @Nonnull final Fleet actor,
                        @Nonnull final Fleet target) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.combatSubPhase = ECombatSubPhase.MISSILE_FIRE_PHASE;
        this.cage = cage;
        combatRound = cage.getCurrentCombatRound();
        this.position = cage.getCurrentStateByFleet(actor).getPosition().clone();
        this.lastPosition = position;
        this.actor = actor;
        this.target = target;
        this.targetPosition = cage.getCurrentStateByFleet(target).getPosition().clone();
        this.initialDistance = position.getDistance(targetPosition).abs();
        final Map<Missile, Integer> amountByType = new HashMap<>();
        actor.getShipsByClass().forEach((shipClass, amountOfShips) -> {
            final Set<AlignedFitting> missileFittings = shipClass.getFittingByType(EWeaponType.MISSILE);
            missileFittings.stream()
                    .filter(alignedFitting -> alignedFitting.getLauncher() != null)
                    .forEach(alignedFitting -> {
                        final Missile missile = alignedFitting.getLauncher().getAmmunitionModule().getMissile();
                        // todo reduce amount by fired missiles
                        final int amountOfMissiles = alignedFitting.getAmount();
                        // setting missiles to the salvo
                        amountByType.merge(missile, amountOfMissiles * amountOfShips, Integer::sum);
                    });
        });
        this.missileSalvoHealthState = new MissileSalvoHealthState(amountByType);
        calculateRangePerCombatRound();
        calculateAttackRange();
        final int sum = amountByType.values().stream().mapToInt(Integer::intValue).sum();
        BattleStaticLogger.logMissileRelease(combatRound, position, actor, target, initialDistance, sum);
        historize();
    }

    /**
     * Handles the {@link ECombatPhase#MISSILE_PHASE} for this particular salvo if it is active when reaching the next stage.
     */
    public void handleMissilePhase() {
        if (missileSalvoHealthState.isActive()) {
            missileSalvoHealthState.clearLosses();
        }
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
    private void calculateRangePerCombatRound() {
        if (missileSalvoHealthState.isActive()) {
            rangePerCombatRound = missileSalvoHealthState.getRangePerCombatRound().abs();
        } else {
            rangePerCombatRound = BigDecimal.ZERO;
        }
    }

    /**
     * Calculates the salvos attack range.
     */
    private void calculateAttackRange() {
        if (missileSalvoHealthState.isActive()) {
            longestOffensiveRange = missileSalvoHealthState.getAttackRange().abs();
        } else {
            longestOffensiveRange = BigDecimal.ZERO;
        }
    }

    /**
     * Handles the {@link ECombatSubPhase#ELOKA_PHASE} for this particular salvo.<br>
     * <p>
     * Handles the impact of the electronic counter measures of the target fleet against the missile salvo.
     */
    @VisibleForTesting
    protected void handleElokaPhase() {
        this.combatSubPhase = ECombatSubPhase.ELOKA_PHASE;
        final FleetRoundState targetsCurrentStateByFleet = cage.getCurrentStateByFleet(target);
        final Orbit targetsPosition = targetsCurrentStateByFleet.getPosition();
        final int elokaRange = target.getElokaRange();
        final BigDecimal distance = position.getDistance(targetsPosition);
        if (BigDecimal.valueOf(elokaRange).compareTo(distance) <= 0) {
            // noop if eloka is not in range
            return;
        }

        final int elokaEffectValue = target.getElokaEffectValue();
        final Map<Missile, Integer> lostByType = new HashMap<>();
        missileSalvoHealthState.getCurrentAmountByType().forEach((missile, amount) -> {
            final int elokaResistance = missile.getElokaResistance();
            int lostCounter = 0;
            for (int i = 1; i <= amount; i++) {
                final boolean isLost = BattleCalculator.calculateElokaImpact(elokaResistance, elokaEffectValue);
                BattleStaticLogger.elokaHits(combatRound, missile, distance, isLost);
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
        lostByType.forEach((missile, lostAmount) -> {
            // removing lost missiles from salvo
            final Integer oldValue = missileSalvoHealthState.getCurrentAmountByType().get(missile);
            final int newValue = oldValue - lostAmount;
            BattleStaticLogger.logElokaHitMissile(combatRound, actor, target, missile, lostAmount, newValue);
            missileSalvoHealthState.setNewMissileAmounts(missile, newValue, oldValue);
        });

        calculateRangePerCombatRound();
        calculateAttackRange();
        historize();
    }

    private void historize() {
        //noinspection RedundantCast
        cage.addHistorizable((MissileSalvo) this);
    }

    /**
     * Handles the {@link ECombatSubPhase#COUNTER_MISSILE_PHASE}.<br>
     * Handles the impact of the counter missile weaponry of the target fleet against the missile salvo.<br>
     * <br>
     * Currently, all counter missile measures will be applied if the salvo is in range.<br>
     * The idea is that missiles are so fast that there is only one shot for every counter measure.<br>
     * In principle, it makes no difference if the counter measure is applied direct after the start or shortly before the impact.
     */
    @VisibleForTesting
    protected void handleCounterMissilePhase() {
        this.combatSubPhase = ECombatSubPhase.COUNTER_MISSILE_PHASE;
        final FleetRoundState targetsCurrentStateByFleet = cage.getCurrentStateByFleet(target);
        final Orbit targetsPosition = targetsCurrentStateByFleet.getPosition();
        final long counterMissileRange = target.getCounterMissileRange();
        final BigDecimal distance = position.getDistance(targetsPosition);
        if (BigDecimal.valueOf(counterMissileRange).compareTo(distance) <= 0) {
            // noop if eloka is not in range
            return;
        }

        final Map<Missile, Integer> lostByType = new HashMap<>();
        final CounterMissileWeaponry counterMissileWeaponry = target.getCounterMissileWeaponry();
        missileSalvoHealthState.getCurrentAmountByType().forEach((missile, amount) -> {
            final int lostCounter = counterMissileWeaponry.calculateDestroyedMissiles(missile, amount);
            if (lostCounter > 0) {
                // memorize losses
                lostByType.put(missile, lostCounter);
            }
        });
        lostByType.forEach((missile, lostAmount) -> {
            // removing lost missiles from salvo
            final Integer oldValue = missileSalvoHealthState.getCurrentAmountByType().get(missile);
            final int newValue = oldValue - lostAmount;
            BattleStaticLogger.logCounterMissileHitMissile(combatRound, actor, missile, lostAmount, newValue);
            missileSalvoHealthState.setNewMissileAmounts(missile, newValue, oldValue);
        });

        calculateRangePerCombatRound();
        calculateAttackRange();
        historize();
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_MOVEMENT_PHASE}.<br>
     * Calculates and runs the new position for the movement of a combat round.
     */
    @VisibleForTesting
    protected void handleMovement() {
        this.combatSubPhase = ECombatSubPhase.MISSILE_MOVEMENT_PHASE;
        if (isInDetonationRange) {
            // noop is already in detonation range
            return;
        }
        final FleetRoundState targetsCurrentStateByFleet = cage.getCurrentStateByFleet(target);
        targetPosition = targetsCurrentStateByFleet.getPosition().clone();
        final BigDecimal distanceToTarget = position.getDistance(targetPosition).abs();
        if (distanceToTarget.compareTo(longestOffensiveRange) <= 0) {
            isInDetonationRange = true;
            return;
        }

        final BigDecimal minimalDistanceToAttack = distanceToTarget.subtract(longestOffensiveRange).abs();
        final BigDecimal distance;
        if (minimalDistanceToAttack.compareTo(rangePerCombatRound) > 0) {
            // use complete movement to track the target
            distance = rangePerCombatRound;
        } else {
            // move to the targets position directly
            distance = distanceToTarget;
            isInDetonationRange = true;
        }
        final Orbit newPos = position.move(EMovementType.GO_TIGHT, distance, targetPosition);
        lastPosition = position.clone();
        position.moveTo(newPos);
        BattleStaticLogger.logHandleMissileMovementS(this);
        historize();
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_FIRE_INCOMING_PHASE} for this particular salvo.<br>
     * Handles the damage application to the target fleet.
     */
    public void detonate() {
        this.combatSubPhase = ECombatSubPhase.MISSILE_FIRE_INCOMING_PHASE;
        final FleetRoundState currentTargetState = cage.getCurrentStateByFleet(target);
        final FleetHealthState targetHealthState = currentTargetState.getFleetHealthState();
        missileSalvoHealthState.getCurrentAmountByType().forEach((missile, missileAmount) -> {
            final long applicableDamage = missile.getWarhead().getDamageValue();
            BattleStaticLogger.logMissileDetonation(getCombatRound(), actor, target, missile, missileAmount);
            for (int i = 1; i <= missileAmount; i++) {
                targetHealthState.applyDamage(applicableDamage, this).ifPresent(warShip -> {
                    final List<Long> alreadyAppliedDamages = appliedDamage.computeIfAbsent(warShip, k -> new ArrayList<>());
                    alreadyAppliedDamages.add(applicableDamage);
                    appliedDamage.put(warShip, alreadyAppliedDamages);
                });
            }
        });
        missileSalvoHealthState.getCurrentAmountByType().clear();
        historize();
    }

    public boolean isInDetonationRange() {
        return isInDetonationRange;
    }

    @Nonnull
    public BigDecimal getRangePerCombatRound() {
        return rangePerCombatRound;
    }

    @Nonnull
    public BigDecimal getLongestOffensiveRange() {
        return longestOffensiveRange;
    }

    public boolean isActive() {
        return missileSalvoHealthState.isActive();
    }

    @Override
    public MissileSalvo clone() {
        final MissileSalvo clone = (MissileSalvo) super.clone();
        clone.combatRound = combatRound.clone();
        clone.position = position.clone();
        clone.lastPosition = lastPosition.clone();
        clone.missileSalvoHealthState = missileSalvoHealthState.clone();
        return clone;
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public ECombatSubPhase getCombatSubPhase() {
        return combatSubPhase;
    }

    @Nonnull
    public Orbit getPosition() {
        return position;
    }

    @Nonnull
    public Orbit getLastPosition() {
        return lastPosition;
    }

    @Nonnull
    public Orbit getTargetPosition() {
        return targetPosition;
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public Fleet getTarget() {
        return target;
    }

    public BigDecimal getInitialDistance() {
        return initialDistance;
    }

    @Nonnull
    public MissileSalvoHealthState getMissileSalvoHealthState() {
        return missileSalvoHealthState;
    }

    @Nonnull
    public Map<WarShip, List<Long>> getAppliedDamage() {
        return appliedDamage;
    }
}
