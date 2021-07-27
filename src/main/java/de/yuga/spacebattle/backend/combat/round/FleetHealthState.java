package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.BattleStaticLogger;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.dto.HitLog;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
        this.warshipHealthStates = fleet.getShips().stream().collect(Collectors.toMap(Function.identity(), WarshipHealthState::new));
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
     * @param damageValue  the damage to apply
     * @param damageDealer the source of the damage
     * @return the attacked war ship
     */
    @Nonnull
    public Optional<WarShip> applyDamage(final long damageValue, @Nonnull final Historizable<? extends Cloneable> damageDealer) {
        Preconditions.checkState(damageValue >= 0, "The damage to apply should be positive.");
        Preconditions.checkNotNull(damageDealer, "damageDealer shouldn't be null!");

        if (warshipHealthStates.isEmpty()) {
            return Optional.empty();
        }

        int numberOfAttackedShip = 0;
        if (warshipHealthStates.size() > 1) {
            numberOfAttackedShip = ThreadLocalRandom.current().nextInt(0, warshipHealthStates.size() - 1);
        }
        final WarShip warShip = new ArrayList<>(warshipHealthStates.keySet()).get(numberOfAttackedShip);
        final WarshipHealthState warshipHealthState = warshipHealthStates.get(warShip);
        if (warshipHealthState.isFightingCapable()) {
            warshipHealthState.applyDamage(damageValue, damageDealer);
        } else {
            return applyDamage(damageValue, damageDealer);
        }
        cleanUp();
        return Optional.of(warshipHealthState.getWarShip());
    }

    /**
     * Cleans up the fighting capable warships to the losses.
     */
    private void cleanUp() {
        final Map<WarShip, WarshipHealthState> losses = warshipHealthStates.entrySet().stream()
                .filter(e -> !e.getValue().isFightingCapable())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        losses.forEach((warShip, warshipHealthState) -> {
            BattleStaticLogger.logLoss(warShip);
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
