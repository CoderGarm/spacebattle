package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.BattleCalculator;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.*;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;

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
public class MissileSalvo extends Historizable<MissileSalvo> {

    /**
     * The cage.
     */
    @Nonnull
    private final Cage cage;

    /**
     * The current position of the target.
     */
    @Nonnull
    private Orbit targetPosition;

    /**
     * The initial distance of this shot.
     */
    @Nonnull
    private final Distance initialDistance;

    @Nonnull
    private final List<MotionProfile> motionProfile = new ArrayList<>();

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
     * The composition of the salvo by missile type, amount and it's current state.
     */
    @Nonnull
    private final MissileSalvoHealthState missileSalvoHealthState;

    /**
     * The distance which can be covered per combat round.
     */
    @Nonnull
    private Distance rangePerCombatRound = Distance.ZERO;

    /**
     * The distance in which the damage can be applied to the target.
     */
    @Nonnull
    private Distance longestOffensiveRange = Distance.ZERO;

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

    private int roundsTravelled = 1;

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
        final FleetRoundState actorsState = cage.getCurrentStateByFleet(this.actor);
        this.targetPosition = cage.getCurrentStateByFleet(target).getPosition().clone();
        this.initialDistance = actorsState.getPosition().getDistance(targetPosition);

        this.motionProfile.add(new MotionProfile(
                actorsState.getCombatRound(),
                actorsState.getAccelerationFor(EModuleType.PROPULSION),
                actorsState.getVelocity(),
                new Direction(actorsState.getPosition(), cage.getCurrentStateByFleet(target).getPosition()),
                actorsState.getPosition()

        ));
        final Map<Missile, Integer> amountByType = new HashMap<>();

        actorsState.getFightingWarShips()
                .filter(WarshipHealthState::isFightingCapable)
                .forEach(w -> w.getFittings().entrySet().stream()
                        // filter active fittings
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(a -> a.getWeaponType() == EWeaponType.MISSILE)
                        .filter(a -> a.getLauncher() != null)
                        .filter(f -> applicableAlignments.contains(f.getWeaponAlignment()))
                        .forEach(alignedFitting -> {
                            final Launcher launcher = alignedFitting.getLauncher();
                            final int amountOfLauncher = alignedFitting.getAmount();

                            final Set<Missile> allowedMissiles = launcher.getAllowedMissiles();
                            final HashSet<Missile> missiles = new HashSet<>(allowedMissiles);
                            missiles.removeIf(m -> !applicableMissiles.contains(m));
                            for (final Missile missile : missiles) {
                                final MissileAmmunitionState missileAmmunitionState = w.getMissileAmmunitionState();
                                final int remainingShots = missileAmmunitionState.getRemainingShots(missile);
                                if (remainingShots <= 0) {
                                    return;
                                }

                                // setting missiles to the salvo
                                if (remainingShots >= amountOfLauncher) {
                                    amountByType.merge(missile, amountOfLauncher, Integer::sum);
                                    missileAmmunitionState.reduce(missile, amountOfLauncher);
                                } else {
                                    amountByType.merge(missile, remainingShots, Integer::sum);
                                    missileAmmunitionState.reduce(missile, remainingShots);
                                }
                            }
                        }));
        this.missileSalvoHealthState = new MissileSalvoHealthState(amountByType);
        calculateRangeForActiveCombatRound();
        calculateAttackRange();
        historize();
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
        if (missileSalvoHealthState.isActive()) {
            this.roundsTravelled++;
        }
    }

    /**
     * This calculates and sets the range per round.<br>
     * Could be useful if the salvo is reduced to the slower missile types.
     */
    private void calculateRangeForActiveCombatRound() {
        if (missileSalvoHealthState.isActive()) {
            final CombatRound currentCombatRound = cage.getCurrentCombatRound();
            final MotionProfile initialMotionProfile = getInitialMotionProfile();
            final CombatRound combatRound = initialMotionProfile.getCombatRound();
            final Velocity initialVelocity = initialMotionProfile.getKinematicInfo().getVelocity();
            // fixme modify the velocity about the direction of the salvo
            final int endurance = (currentCombatRound.getNo() - combatRound.getNo()) * CombatRound.COMBAT_ROUND_DURATION;
            rangePerCombatRound = missileSalvoHealthState.getRangePerCombatRound(initialVelocity, endurance);
        } else {
            rangePerCombatRound = Distance.ZERO;
        }
    }

    /**
     * Calculates the salvos attack range.
     */
    private void calculateAttackRange() {
        if (missileSalvoHealthState.isActive()) {
            longestOffensiveRange = missileSalvoHealthState.getAttackRange();
        } else {
            longestOffensiveRange = Distance.ZERO;
        }
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
        final Distance distance = getPosition().getDistance(targetsPosition);
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

        calculateRangeForActiveCombatRound();
        calculateAttackRange();
        cage.logMessage("Eloka attacked " + Integer.toHexString(hashCode())
                + " and killed " + lostByType.values().stream().mapToInt(Integer::intValue).sum()
                + " (" + missileSalvoHealthState.getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum()
                + " left) against " + target.getOwner().getUsername());
    }

    private void historize() {
        cage.historizeMissileSalvo(this);
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
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final Orbit targetsPosition = targetsState.getPosition();
        final Distance counterMissileRange = targetsState.getCounterMissileRange();
        final Distance distance = getPosition().getDistance(targetsPosition);
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

        calculateRangeForActiveCombatRound();
        calculateAttackRange();
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
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        if (targetsCurrentStateByFleet.getFleetHealthState().isNotFightingCapable()) {
            executeEffectiveDetonation();
            return;
        }

        targetPosition = targetsCurrentStateByFleet.getPosition().clone();
        final Orbit position = getPosition();
        final Distance distanceToTarget = position.getDistance(targetPosition);
        if (distanceToTarget.compareTo(longestOffensiveRange) <= 0) {
            isInDetonationRange = true;
            return;
        }

        final Distance minimalDistanceToAttack = distanceToTarget.subtract(longestOffensiveRange);
        final Distance distance;
        if (minimalDistanceToAttack.compareTo(rangePerCombatRound) > 0) {
            // use complete movement to track the target
            distance = rangePerCombatRound;
        } else {
            // move to the targets position directly
            distance = distanceToTarget;
            isInDetonationRange = true;
        }

        final MotionProfile latestMotionProfile = getLatestMotionProfile();
        final KinematicInfo kinematicInfo = latestMotionProfile.getKinematicInfo();

        final Orbit movedTo = position.move(EMovementType.REDUCE_DISTANCE, distance, targetPosition);
        this.motionProfile.add(new MotionProfile(
                currentCombatRound,
                kinematicInfo.getAcceleration(),
                kinematicInfo.getVelocity().getVelocityByAcceleration(kinematicInfo.getAcceleration(), CombatRound.COMBAT_ROUND),
                new Direction(position, targetPosition),
                movedTo
        ));

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

    @Nonnull
    public List<MotionProfile> getMotionProfile() {
        return motionProfile;
    }

    @Nonnull
    public MotionProfile getInitialMotionProfile() {
        return this.motionProfile.get(0);
    }

    @Nonnull
    public MotionProfile getLatestMotionProfile() {
        Preconditions.checkState(!this.motionProfile.isEmpty(), "motionProfile is unfortunately empty");
        return this.motionProfile.stream().reduce((o1, o2) -> o1.compareTo(o2) < 0 ? o1 : o2).get();
    }

    @Nonnull
    public Orbit getPosition() {
        Preconditions.checkState(!this.motionProfile.isEmpty(), "motionProfile is unfortunately empty");
        return this.motionProfile.stream().reduce((o1, o2) -> o1.compareTo(o2) < 0 ? o1 : o2).map(m -> m.getKinematicInfo().getPosition()).get();
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return getLatestMotionProfile().getCombatRound();
    }

    @Nonnull
    public Orbit getLastPosition() {
        if (motionProfile.size() > 1) {
            return motionProfile.get(motionProfile.size() - 2).getKinematicInfo().getPosition();
        }
        return motionProfile.get(motionProfile.size() - 1).getKinematicInfo().getPosition();
    }

    private void executeEffectiveDetonation() {
        missileSalvoHealthState.getCurrentAmountByType().clear();
    }

    public boolean isInDetonationRange() {
        return isInDetonationRange;
    }

    @Nonnull
    public Distance getRangePerCombatRound() {
        return rangePerCombatRound;
    }

    @Nonnull
    public Distance getLongestOffensiveRange() {
        return longestOffensiveRange;
    }

    public boolean isActive() {
        return missileSalvoHealthState.isActive();
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

    @Nonnull
    public Distance getInitialDistance() {
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

    @Nullable
    public EDamageResult getResult() {
        return result;
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

    public int roundsTravelled() {
        return roundsTravelled;
    }
}
