package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
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
@Table(name = "warshipHealthState")
@AttributeOverride(name = "id", column = @Column(name = "idWarshipHealthState"))
public class WarshipHealthState extends AbstractEntityKey {

    /**
     * The war ship which is this state for.
     */
    @Nonnull
    @OneToOne
    @JoinColumn(name = "idWarship")
    private WarShip warShip;

    /**
     * The state of the hull in percentages. If zero it is destroyed.
     */
    private int hullState;

    /**
     * The state of the armor in percentages. If zero it is destroyed.
     */
    private int armorState;

    /**
     * The state of the sidewall in percentages. If zero it is destroyed.
     */
    private int sidewallState;

    /**
     * The state of the propulsion system in percentages. If zero it is destroyed.
     */
    private int propulsionState;

    /**
     * The state of the electronic warfare systems in percentages. If zero it is destroyed.
     */
    private int elokaState;

    /**
     * Holds the destroyed fittings as substrate from the ship class' fittings.
     */
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activeFittings", joinColumns = @JoinColumn(name = "idWarshipHealthState"))
    private final Set<AlignedFitting> activeFittings = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "idMissile")
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "remainingShots", joinColumns = @JoinColumn(name = "idMissile"))
    private final Map<Missile, Integer> remainingShots = new HashMap<>();

    public WarshipHealthState() {
    }

    public WarshipHealthState(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        this.warShip = warshipHealthState.getWarShip();
        this.armorState = warshipHealthState.getArmorState();
        this.hullState = warshipHealthState.getHullState();
        this.sidewallState = warshipHealthState.getSidewallState();
        this.propulsionState = warshipHealthState.getPropulsionState();
        this.elokaState = warshipHealthState.getElokaState();

        this.activeFittings.addAll(warshipHealthState.getActiveFittings());

        remainingShots.putAll(warshipHealthState.getMissileAmmunitionState().getRemainingShots());
    }

    @Nonnull
    public WarShip getWarShip() {
        return warShip;
    }

    public int getHullState() {
        return hullState;
    }

    public int getArmorState() {
        return armorState;
    }

    public int getSidewallState() {
        return sidewallState;
    }

    public int getPropulsionState() {
        return propulsionState;
    }

    public int getElokaState() {
        return elokaState;
    }

    @Nonnull
    public Set<AlignedFitting> getActiveFittings() {
        return activeFittings;
    }

    @Nonnull
    public Map<Missile, Integer> getRemainingShots() {
        return remainingShots;
    }

    /**
     * States if the war ship has any active weapon left.
     *
     * @return <code>true</code> if the ship can fight, <code>false</code> otherwise
     */
    public boolean isFightingCapable() {
        return hullState > 0 && armorState > 0 && sidewallState > 0 && propulsionState > 0 && elokaState > 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final WarshipHealthState that = (WarshipHealthState) o;

        return new EqualsBuilder().append(warShip, that.warShip).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(warShip).toHashCode();
    }
}
