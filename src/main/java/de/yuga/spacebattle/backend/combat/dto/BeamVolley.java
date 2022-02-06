package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.BeamState;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.BURST_ON_SIDEWALL;
import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.DAMAGE_APPLIED;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.SIDEWALL_PROTECTION;

/**
 * Represents a salvo of direct hit weapons.
 */
public class BeamVolley extends Historizable<BeamVolley> implements Cloneable {

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
    private ECombatSubPhase combatSubPhase;

    /**
     * The source of the salvo.
     */
    @Nonnull
    private final Fleet actor;

    /**
     * The target of the salvo.
     */
    @Nonnull
    private final Fleet target;

    /**
     * The distance of this shot.
     */
    private final BigDecimal distance;

    /**
     * The damage and the targets which were affected by the damage.
     */
    @Nonnull
    private final Map<WarShip, List<Long>> appliedDamage = new HashMap<>();

    @Nonnull
    private final List<BeamState> firedShots = new ArrayList<>();

    /**
     * The result of this salvo.
     */
    @Nullable
    private EDamageResult result;

    /**
     * Creates a volley of all direct beam weapons.
     *
     * @param cage   the cage
     * @param actor  the acting fleet
     * @param target the target
     */
    public BeamVolley(@Nonnull final Cage cage,
                      @Nonnull final Fleet actor,
                      @Nonnull final Fleet target) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.combatSubPhase = ECombatSubPhase.BEAM_FIRE_PHASE;
        this.combatRound = cage.getCurrentCombatRound();
        this.cage = cage;
        this.actor = actor;
        this.target = target;
        final FleetRoundState actorsState = cage.getCurrentStateByFleet(actor);
        this.distance = actorsState.getPosition().getDistance(cage.getCurrentStateByFleet(target).getPosition());

        final EMovementType actorsMovementType = actorsState.getMovementType();
        actorsState
                .getFleetHealthState()
                .getWarshipHealthStates()
                .values().forEach(warshipHealthState -> {
                    // every ship shoots against another one and concentrates fire
                    final WarShip targetedWarShip = cage.getRandomActiveWarShipOfFleet(target);
                    if (targetedWarShip == null) {
                        // noop - no targets left
                        return;
                    }
                    warshipHealthState.getActiveFittingsByWeaponType(EWeaponType.BEAM).stream()
                            .filter(b -> b.getWeapon() != null)
                            .filter(f -> f.getWeaponAlignment().isAssignableFromMovementType(actorsMovementType))
                            .forEach(alignedFitting -> {
                                final Weapon weapon = alignedFitting.getWeapon();
                                final int amountDamageEmitter = weapon.getAmountDamageEmitter();
                                final long applicableDamage = weapon.getEffectValue();
                                for (int i = 1; i <= amountDamageEmitter; i++) {
                                    // todo implement chance to hit for beans - currently it is 100 %
                                    firedShots.add(new BeamState(warshipHealthState.getWarShip(), targetedWarShip, applicableDamage, BigDecimal.ONE));
                                }
                            });
                });

        historize();
    }

    private void historize() {
        //noinspection RedundantCast
        cage.addHistorizable((BeamVolley) this);
    }

    /**
     * Handles the {@link ECombatSubPhase#BEAM_FIRE_INCOMING_PHASE}.<br>
     * Applies the damage to the target.
     */
    public void applyDamage() {
        this.combatSubPhase = ECombatSubPhase.BEAM_FIRE_INCOMING_PHASE;
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        if (SIDEWALL_PROTECTION != targetsState.getMovementType()) {
            result = BURST_ON_SIDEWALL;
        } else {
            final FleetHealthState targetHealthState = targetsState.getFleetHealthState();
            firedShots.forEach(beamState -> {
                final BigDecimal chanceToHit = beamState.getChanceToHit();
                // chance to hit is here pars pro toto for every hit
                final long applicableDamage = chanceToHit.multiply(BigDecimal.valueOf(beamState.getDamageValue()), DistanceCalculator.MATH_CONTEXT_MORE_PRECISION).longValue();
                final WarShip targetedWarship = beamState.getTarget();
                targetHealthState.applyDamage(targetedWarship, applicableDamage, this).ifPresent(warShip -> {
                    final List<Long> alreadyAppliedDamages = appliedDamage.computeIfAbsent(warShip, k -> new ArrayList<>());
                    alreadyAppliedDamages.add(applicableDamage);
                    appliedDamage.put(warShip, alreadyAppliedDamages);
                });
            });
            result = DAMAGE_APPLIED;
        }
        historize();
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
    public Fleet getTarget() {
        return target;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    @Nonnull
    public ECombatSubPhase getCombatSubPhase() {
        return combatSubPhase;
    }

    @Nonnull
    public Map<WarShip, List<Long>> getAppliedDamage() {
        return appliedDamage;
    }

    @Nullable
    public EDamageResult getResult() {
        return result;
    }

    @Override
    public BeamVolley clone() {
        final BeamVolley clone = (BeamVolley) super.clone();
        clone.combatRound = combatRound.clone();
        return clone;
    }
}
