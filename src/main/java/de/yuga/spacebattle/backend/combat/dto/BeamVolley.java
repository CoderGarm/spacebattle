package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.BattleStaticLogger;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.distance = cage.getCurrentStateByFleet(actor).getPosition().getDistance(cage.getCurrentStateByFleet(target).getPosition());
        final Map<ShipClass, Integer> amountPerShipsClass = actor.getShipsByClass();
        amountPerShipsClass.forEach((shipClass, amount) -> {
            BattleStaticLogger.logBeamVolleyRelease(combatRound, actor, shipClass, amount);
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
        final FleetRoundState targetState = cage.getCurrentStateByFleet(target);
        final FleetHealthState targetHealthState = targetState.getFleetHealthState();

        // todo implement change to hit

        final Map<ShipClass, Integer> amountPerShipsClass = new HashMap<>(actor.getShipsByClass());
        final Map<ShipClass, Long> damagePerShipClass = amountPerShipsClass.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    final BigDecimal subtract = distance.subtract(BigDecimal.TEN);
                    final BigDecimal add = distance.add(BigDecimal.TEN);
                    return e.getKey().getDamagePerRangePerWeaponType(subtract, add, EWeaponType.BEAM);
                }));
        // removing all classes which cannot apply damage
        final List<ShipClass> withoutApplicableDamage = damagePerShipClass.entrySet().stream().filter(e -> e.getValue() <= 0).map(Map.Entry::getKey).collect(Collectors.toList());
        withoutApplicableDamage.forEach(damagePerShipClass.keySet()::remove);
        withoutApplicableDamage.forEach(amountPerShipsClass.keySet()::remove);

        amountPerShipsClass.forEach((shipClass, amount) -> {
            // todo every volley hits - chance to hit
            final long applicableDamage = damagePerShipClass.get(shipClass);
            BattleStaticLogger.logBeamVolleyHit(combatRound, actor, target, shipClass, applicableDamage);
            // todo apply damage weapon by weapon
            for (int i = 1; i <= amount; i++) {
                targetHealthState.applyDamage(applicableDamage, this).ifPresent(warShip -> {
                    final List<Long> alreadyAppliedDamages = appliedDamage.computeIfAbsent(warShip, k -> new ArrayList<>());
                    alreadyAppliedDamages.add(applicableDamage);
                    appliedDamage.put(warShip, alreadyAppliedDamages);
                });
            }
        });
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

    @Override
    public BeamVolley clone() {
        final BeamVolley clone = (BeamVolley) super.clone();
        clone.combatRound = combatRound.clone();
        return clone;
    }
}
