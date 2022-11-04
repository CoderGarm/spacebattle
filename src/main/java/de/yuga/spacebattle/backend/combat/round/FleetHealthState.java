package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.enums.EDamageImpact;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FleetHealthState implements Cloneable {

    @Nonnull
    private final Fleet fleet;

    @Nonnull
    private Map<WarShip, WarshipHealthState> warshipHealthStates;

    @Nonnull
    private Map<WarShip, WarshipHealthState> losses = new HashMap<>();

    public FleetHealthState(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.fleet = fleet;
        this.warshipHealthStates = fleet.getAliveShips().stream().collect(Collectors.toMap(Function.identity(), WarshipHealthState::new));
    }

    /**
     * States if the fleet contains a ship which is capable of holding lives.
     *
     * @return <code>true</code> if there is a hull present which could hold an atmosphere, <code>false</code> otherwise
     */
    public boolean isAlive() {
        return warshipHealthStates.values().stream().anyMatch(WarshipHealthState::isAlive);
    }

    /**
     * States if the war ship has no active weapon left.
     *
     * @return <code>true</code> if the ship can't fight, <code>false</code> otherwise
     */
    public boolean isNotFightingCapable() {
        return warshipHealthStates.values().stream().noneMatch(WarshipHealthState::isFightingCapable);
    }

    /**
     * States if the war ship has any active weapon left.
     *
     * @return <code>true</code> if the ship can fight, <code>false</code> otherwise
     */
    public boolean isFightingCapable() {
        return warshipHealthStates.values().stream().anyMatch(WarshipHealthState::isFightingCapable);
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public Map<WarShip, WarshipHealthState> getWarshipHealthStates() {
        return warshipHealthStates;
    }

    @Nonnull
    public Map<WarShip, WarshipHealthState> getLosses() {
        return losses;
    }

    /**
     * Applies the given damage to a random war ship.<br>
     * <br>
     * If this ship is destroyed
     *
     * @param target       the targeted ship
     * @param damageValue  the damage to apply
     * @param damageDealer the source of the damage
     * @return the attacked war ship
     */
    @Nonnull
    public Optional<WarShip> applyDamage(@Nonnull final WarShip target,
                                         final long damageValue,
                                         @Nonnull final Historizable<? extends Cloneable> damageDealer) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkState(damageValue >= 0, "The damage to apply should be positive.");
        Preconditions.checkNotNull(damageDealer, "damageDealer shouldn't be null!");

        if (warshipHealthStates.isEmpty()) {
            return Optional.empty();
        }

        final WarshipHealthState warshipHealthState = warshipHealthStates.get(target);
        if (warshipHealthState != null && warshipHealthState.isFightingCapable()) {
            warshipHealthState.applyDamage(damageValue, damageDealer);
        } else {
            final WarShip secondTargetedWarship = getRandomSecondTarget();
            return applyDamage(secondTargetedWarship, damageValue, damageDealer);
        }
        cleanUp();
        return Optional.of(warshipHealthState.getWarShip());
    }

    /**
     * Returns a randomly selected second target if the given one if not available.
     *
     * @return a target to attack
     */
    @Nullable
    private WarShip getRandomSecondTarget() {
        if (warshipHealthStates.isEmpty()) {
            return null;
        }
        int numberOfAttackedShip = 0;
        if (warshipHealthStates.size() > 1) {
            numberOfAttackedShip = ThreadLocalRandom.current().nextInt(0, warshipHealthStates.size() - 1);
        }
        return new ArrayList<>(warshipHealthStates.keySet()).get(numberOfAttackedShip);
    }

    /**
     * Cleans up the fighting capable warships from the active ones.
     */
    private void cleanUp() {
        final Map<WarShip, WarshipHealthState> losses = warshipHealthStates.entrySet().stream()
                .filter(e -> !e.getValue().isFightingCapable())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        losses.forEach((warShip, warshipHealthState) -> {
            warshipHealthStates.remove(warShip);
            this.losses.put(warShip, warshipHealthState);
        });
    }

    /**
     * Returns the fleets hit Log up to the current state.
     *
     * @return the overall hit log
     */
    public Map<WarShip, List<HitLog>> getHitLogs() {
        final Map<WarShip, List<HitLog>> hitLogsOfActive = warshipHealthStates.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getHitLog()));
        final Map<WarShip, List<HitLog>> hitLogsOfLoss = losses.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getHitLog()));
        final Set<WarShip> warShips = new HashSet<>(hitLogsOfActive.keySet());
        warShips.addAll(hitLogsOfLoss.keySet());
        final Map<WarShip, List<HitLog>> result = new HashMap<>();
        warShips.forEach(warShip -> {
            final List<HitLog> activeLogs = hitLogsOfActive.computeIfAbsent(warShip, k -> new ArrayList<>());
            final List<HitLog> lossLogs = hitLogsOfLoss.computeIfAbsent(warShip, k -> new ArrayList<>());
            final List<HitLog> allLogs = new ArrayList<>(activeLogs);
            if (!allLogs.isEmpty()) {
                allLogs.addAll(lossLogs);
                result.put(warShip, allLogs);
            }
        });
        return result;
    }

    /**
     * Returns if there are shots left in this fleet.
     *
     * @return <code>true</code> if there are missiles remaining, <code>false</code> otherwise
     */
    public boolean hasShotsLeft() {
        return warshipHealthStates.values().stream().anyMatch(w -> w.getMissileAmmunitionState().hasShotsLeft());
    }

    /**
     * Estimates the likely losses which can be expected by the given damage input.
     *
     * @param missileSalvos the hitting missiles
     * @param beamVolleys   the hitting beams
     * @return the estimation of losses in relation to the current fleet
     */
    public EDamageImpact estimateLosses(@Nonnull final List<MissileSalvo> missileSalvos, @Nonnull final List<BeamVolley> beamVolleys) {
        Preconditions.checkNotNull(missileSalvos, "missileSalvos shouldn't be null!");
        Preconditions.checkNotNull(beamVolleys, "beamVolleys shouldn't be null!");

        final List<ApplicableDamage> damages = beamVolleys.stream().map(BeamVolley::getApplicableDamage).flatMap(Collection::stream).collect(Collectors.toList());
        damages.addAll(missileSalvos.stream().map(MissileSalvo::getApplicableDamage).flatMap(Collection::stream).collect(Collectors.toList()));
        final BigDecimal fullDamagePotential = damages.stream().map(a -> BigDecimal.valueOf(a.getEffectiveDamage())).reduce(BigDecimal.ZERO, BigDecimal::add);

        final Map<WarShip, Integer> defensiveByShip = warshipHealthStates.values().stream()
                .collect(Collectors.toMap(WarshipHealthState::getWarShip, w -> w.getArmorState() + w.getSidewallState()));

        final List<WarShip> destroyed = new ArrayList<>();
        final AtomicReference<BigDecimal> atomicReference = new AtomicReference<>(fullDamagePotential);
        defensiveByShip.forEach((warShip, defenseValue) -> {
            final BigDecimal attack = atomicReference.get();
            final BigDecimal def = BigDecimal.valueOf(defenseValue);
            if (def.compareTo(attack) <= 0) {
                destroyed.add(warShip);
            }
            final BigDecimal leftDamage = attack.subtract(def);
            atomicReference.set(leftDamage);
        });

        return EDamageImpact.getImpactByLossRatio(destroyed.size(), defensiveByShip.keySet().size());
    }

    @Override
    public FleetHealthState clone() {
        try {
            final FleetHealthState clone = (FleetHealthState) super.clone();
            clone.warshipHealthStates = warshipHealthStates.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
            clone.losses = losses.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof FleetHealthState)) return false;

        final FleetHealthState that = (FleetHealthState) o;

        return new EqualsBuilder().append(fleet, that.fleet).append(warshipHealthStates, that.warshipHealthStates).append(losses, that.losses).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(fleet).append(warshipHealthStates).append(losses).toHashCode();
    }
}
