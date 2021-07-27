package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissileSalvoHealthState extends Historizable<MissileSalvoHealthState> implements Cloneable {

    /**
     * The initial composition of the salvo by missile type and amount.
     */
    @Nonnull
    private Map<Missile, Integer> initialAmountByType;

    /**
     * The current composition of the salvo by missile type and amount.
     */
    @Nonnull
    private Map<Missile, Integer> currentAmountByType;

    /**
     * The composition of the salvo by missile type and amount.
     */
    @Nonnull
    private Map<Missile, Integer> lossesByType;

    public MissileSalvoHealthState(@Nonnull final Map<Missile, Integer> initialAmountByType) {
        Preconditions.checkNotNull(initialAmountByType, "initialAmountByType shouldn't be null!");

        this.initialAmountByType = new HashMap<>(initialAmountByType);
        this.currentAmountByType = new HashMap<>(initialAmountByType);
        this.lossesByType = initialAmountByType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> 0));
    }

    @Nonnull
    public Map<Missile, Integer> getInitialAmountByType() {
        return initialAmountByType;
    }

    @Nonnull
    public Map<Missile, Integer> getCurrentAmountByType() {
        return currentAmountByType;
    }

    @Nonnull
    public Map<Missile, Integer> getLossesByType() {
        return lossesByType.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Checks if the salvo has missiles in the air.
     *
     * @return <code>true</code> if the salvo is action and dangerous, <code>false</code> otherwise
     */
    public boolean isActive() {
        return currentAmountByType.values().stream().anyMatch(amount -> amount > 0);
    }

    /**
     * Calculates the salvos attack range.
     *
     * @return the warheads range
     */
    @Nonnull
    public BigDecimal getAttackRange() {
        if (!isActive()) {
            return BigDecimal.ZERO;
        }
        final List<BigDecimal> damageProjectionRanges = currentAmountByType.keySet().stream()
                .map(missile -> missile.getWarhead().getDamageProjectionRange())
                .sorted()
                .collect(Collectors.toList());
        // get longest damage projection range to detect if it is in range
        return damageProjectionRanges.get(damageProjectionRanges.size() - 1);
    }

    /**
     * This calculates and sets the range per round.<br>
     * Could be useful if the salvo is reduced to the slower missile types.
     */
    public BigDecimal getRangePerCombatRound() {
        if (!isActive()) {
            return BigDecimal.ZERO;
        }
        final List<BigDecimal> rangesPerCombatRoundAsc = currentAmountByType.keySet().stream()
                .map(Missile::getRangePerCombatRound)
                .sorted()
                .collect(Collectors.toList());
        return rangesPerCombatRoundAsc.get(0);
    }

    public void setNewMissileAmounts(final Missile missile, final int newValue, final Integer oldValue) {
        if (newValue > 0) {
            currentAmountByType.replace(missile, oldValue, newValue);
        } else {
            currentAmountByType.put(missile, 0);
        }
        final Integer oldLosses = lossesByType.get(missile);
        lossesByType.replace(missile, oldLosses, oldLosses + oldValue - newValue);
    }

    @Override
    @SuppressWarnings("BoxingBoxedValue")
    public MissileSalvoHealthState clone() {
        final MissileSalvoHealthState clone = (MissileSalvoHealthState) super.clone();
        clone.initialAmountByType = initialAmountByType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Integer.valueOf(e.getValue())));
        clone.currentAmountByType = currentAmountByType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Integer.valueOf(e.getValue())));
        clone.lossesByType = lossesByType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Integer.valueOf(e.getValue())));
        return clone;
    }

    public void clearLosses() {
        lossesByType = initialAmountByType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> 0));
    }
}
