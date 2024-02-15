package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.BeamState;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;
import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.BURST_ON_IMPELLER_WEDGE;
import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.DAMAGE_APPLIED;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.IMPELLER_WEDGE_PROTECTION;

/**
 * Represents a salvo of direct hit weapons.
 */
public class BeamVolley extends DamageDealer {

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
    private final CombatRound combatRound;

    /**
     * The current phase.
     */
    @Nonnull
    private ECombatSubPhase combatSubPhase; // fixme remove it - not longer symmetrical

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
    @Nonnull
    private final Distance initialDistance;

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
        this.initialDistance = actorsState.getPosition().getDistance(cage.getCurrentStateByFleet(target).getPosition());

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
                                    // todo implement chance to hit for beams - currently it is 100 %
                                    firedShots.add(new BeamState(warshipHealthState.getWarShip(), targetedWarShip, applicableDamage, BigDecimal.ONE));
                                }
                            });
                });
    }

    /**
     * Handles the {@link ECombatSubPhase#BEAM_FIRE_INCOMING_PHASE}.<br>
     * Applies the damage to the target.
     */
    public void applyDamage() {
        this.combatSubPhase = ECombatSubPhase.BEAM_FIRE_INCOMING_PHASE;
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        if (IMPELLER_WEDGE_PROTECTION == targetsState.getMovementType()) {
            result = BURST_ON_IMPELLER_WEDGE;
        } else {
            final FleetHealthState targetHealthState = targetsState.getFleetHealthState();
            firedShots.forEach(beamState -> {
                final BigDecimal chanceToHit = beamState.getChanceToHit();
                // chance to hit is here pars pro toto for every hit
                final long applicableDamage = chanceToHit.multiply(BigDecimal.valueOf(beamState.getDamageValue()), MC_HU).longValue();
                final WarShip targetedWarship = beamState.getTarget();
                targetHealthState.applyDamage(targetedWarship, applicableDamage, this).ifPresent(warShip -> {
                    final List<Long> alreadyAppliedDamages = appliedDamage.computeIfAbsent(warShip, k -> new ArrayList<>());
                    alreadyAppliedDamages.add(applicableDamage);
                    appliedDamage.put(warShip, alreadyAppliedDamages);
                });
            });
            result = DAMAGE_APPLIED;
        }
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

    @Nonnull
    public Distance getInitialDistance() {
        return initialDistance;
    }

    @Nonnull
    public List<BeamState> getFiredShots() {
        return firedShots;
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

    /**
     * Returns the possible damage which can be applied by this volley.
     *
     * @return the damage potential
     */
    @Nonnull
    public List<ApplicableDamage> getApplicableDamage() {
        return firedShots.stream().map(ApplicableDamage::new).collect(Collectors.toList());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final BeamVolley that = (BeamVolley) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(combatRound, that.combatRound).append(actor, that.actor).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(combatRound).append(actor).toHashCode();
    }
}
