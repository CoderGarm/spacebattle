package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.EModuleType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "warshipHealthStateSnapshot")
@AttributeOverride(name = "id", column = @Column(name = "idWarshipHealthStateSnapshot"))
public class WarshipHealthStateSnapshot extends Operationable implements WarshipHealthStateAccessor {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idBattleReport")
    private BattleReport battleReport;

    /**
     * The war ship which is this snap for.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idWarship")
    private WarShip warShip;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idFleetSnapshot")
    private FleetSnapshot fleetSnapshot;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "warshipCapabilitiesSnapshot", joinColumns = @JoinColumn(name = "idWarshipHealthStateSnapshot"))
    private final Set<CapabilityValue> capabilities = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activeFittingsSnapshot", joinColumns = @JoinColumn(name = "idWarshipHealthStateSnapshot"))
    private final Set<AlignedFitting> activeFittings = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idMissile", referencedColumnName = "idMissile")
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "remainingShotsSnapshot", joinColumns = @JoinColumn(name = "idWarshipHealthStateSnapshot"))
    private final Map<Missile, Integer> remainingShots = new HashMap<>();

    @Column(columnDefinition = "bit not null default true")
    private boolean isFightingCapable = true;

    public WarshipHealthStateSnapshot() {
    }

    public WarshipHealthStateSnapshot(@Nonnull final FleetSnapshot fleetSnapshot, @Nonnull final BattleReport battleReport, @Nonnull final WarShip warship) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
        Preconditions.checkNotNull(warship, "warship must not be empty");

        this.fleetSnapshot = fleetSnapshot;
        this.battleReport = battleReport;
        this.warShip = warship;
        final WarshipHealthState healthState = warship.getWarshipHealthState();
        this.capabilities.addAll(new SpacecraftCalculator().getCapabilityValues(healthState));
        this.activeFittings.addAll(healthState.getActiveFittings());
        this.remainingShots.putAll(healthState.getRemainingShots());
        this.isFightingCapable = healthState.isFightingCapable();
    }

    @Nonnull
    public BattleReport getBattleReport() {
        return battleReport;
    }

    public double getStateByAsDouble(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().doubleValue()).reduce(0D, Double::sum);
    }

    public int getStateByAsInt(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().intValue()).reduce(0, Integer::sum);
    }

    @Override
    @Nonnull
    public WarShip getWarShip() {
        return warShip;
    }

    @Nonnull
    public FleetSnapshot getFleetSnapshot() {
        return fleetSnapshot;
    }

    @Nonnull
    public Set<AlignedFitting> getActiveFittings() {
        return activeFittings;
    }

    @Nonnull
    public Map<Missile, Integer> getRemainingShots() {
        return remainingShots;
    }

    public void setFightingCapable(final boolean fightingCapable) {
        isFightingCapable = fightingCapable;
    }

    /**
     * States if the war ship has any active weapon left.
     *
     * @return <code>true</code> if the ship can fight, <code>false</code> otherwise
     */
    public boolean isFightingCapable() {
        return isFightingCapable;
    }

    @Nonnull
    public Set<CapabilityValue> getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final WarshipHealthStateSnapshot that = (WarshipHealthStateSnapshot) o;

        return new EqualsBuilder().append(battleReport, that.battleReport).append(warShip, that.warShip).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(battleReport).append(warShip).toHashCode();
    }
}
