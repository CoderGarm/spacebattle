package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "warshipHealthState")
@AttributeOverride(name = "id", column = @Column(name = "idWarshipHealthState"))
public class WarshipHealthState extends AbstractEntityKey implements WarshipHealthStateAccessor {

    /**
     * The war ship which is this state for.
     */
    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idWarship")
    private WarShip warShip;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "warshipCapabilities", joinColumns = @JoinColumn(name = "idWarshipHealthState"))
    private final Set<CapabilityValue> capabilities = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activeFittings", joinColumns = @JoinColumn(name = "idWarshipHealthState"))
    private final Set<AlignedFitting> activeFittings = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idMissile", referencedColumnName = "idMissile")
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "remainingShots", joinColumns = @JoinColumn(name = "idWarshipHealthState"))
    private final Map<Missile, Integer> remainingShots = new HashMap<>();

    @Column(columnDefinition = "boolean not null default true")
    private boolean isFightingCapable = true;

    public WarshipHealthState() {
    }

    public WarshipHealthState(@Nonnull final WarShip warship) {
        Preconditions.checkNotNull(warship, "warship must not be empty");

        final ShipClass shipClass = warship.getShipClass();
        this.warShip = warship;
        this.capabilities.addAll(new SpacecraftCalculator().getCapabilityValues(shipClass));
        this.activeFittings.addAll(shipClass.getFittings());

        final Map<Missile, Integer> shotsPerMissile = shipClass.getAmmunitionFittings().stream()
                .collect(Collectors.groupingBy(AmmunitionFitting::getMissile,
                        Collectors.mapping(AmmunitionFitting::getAmount, Collectors.reducing(0, Integer::sum))));

        this.remainingShots.putAll(shotsPerMissile);
    }

    public void update(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        final Set<CapabilityValue> capabilityValues = new SpacecraftCalculator().getCapabilityValues(warshipHealthState);
        for (final CapabilityValue newCap : capabilityValues) {
            final CapabilityValue oldCap = this.capabilities.stream().filter(newCap::equals).findFirst().orElse(null);
            if (oldCap != null) {
                oldCap.setValue(newCap.getValue());
            } else {
                this.capabilities.add(newCap);
            }
        }
        this.isFightingCapable = warshipHealthState.isFightingCapable();
        this.activeFittings.clear();
        this.activeFittings.addAll(warshipHealthState.getActiveFittings());
        this.remainingShots.clear();
        this.remainingShots.putAll(warshipHealthState.getMissileAmmunitionState().getRemainingShots());
    }

    @Override
    public double getStateByAsDouble(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().doubleValue()).reduce(0D, Double::sum);
    }

    @Override
    public int getStateByAsInt(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().intValue()).reduce(0, Integer::sum);
    }

    @Override
    @Nonnull
    public WarShip getWarShip() {
        return warShip;
    }

    @Override
    @Nonnull
    public Set<AlignedFitting> getActiveFittings() {
        return activeFittings;
    }

    @Override
    @Nonnull
    public Map<Missile, Integer> getRemainingShots() {
        return remainingShots;
    }

    @Override
    public void setFightingCapable(final boolean fightingCapable) {
        isFightingCapable = fightingCapable;
    }

    /**
     * States if the war ship has any active weapon left.
     *
     * @return <code>true</code> if the ship can fight, <code>false</code> otherwise
     */
    @Override
    public boolean isFightingCapable() {
        return isFightingCapable;
    }

    /**
     * States if the war ship is alive.
     *
     * @return <code>true</code> if the ship can is probably a hulk, but not destroyed, <code>false</code> otherwise
     */
    @Override
    public boolean isAlive() {
        return warShip.isAlive();
    }

    @Override
    public boolean isOperational() {
        return warShip.isOperational();
    }

    @Override
    @Nonnull
    public Set<CapabilityValue> getCapabilities() {
        return capabilities;
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

    /**
     * Checks if the health state has a difference from the untouched state of a fresh warship.
     *
     * @param reference the reference
     * @return <code>true</code> if there is a relevant difference, <code>false</code> otherwise
     */
    public boolean needsRepair(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState reference) {
        Preconditions.checkNotNull(reference, "reference must not be empty");

        if (!this.getWarShip().equals(reference.getWarShip())) {
            throw new NotifyWebUserException("The warship health states can only be checked for the same individual ships.");
        }

        final boolean differState = !(getStateByAsInt(EModuleType.ARMOR) == reference.getArmorState()
                && getStateByAsInt(EModuleType.ELECTRONIC_WARFARE) == reference.getElokaState()
                && getStateByAsInt(EModuleType.SIDEWALL) == reference.getSidewallState()
                && getStateByAsInt(EModuleType.PROPULSION) == reference.getPropulsionState());

        final boolean differInActivityState = isFightingCapable || reference.isFightingCapable();
        return differState || differInActivityState;
    }

    /**
     * Checks if the health state has a difference from the untouched state of a fresh warship.
     *
     * @param reference the reference
     * @return <code>true</code> if there is a relevant difference, <code>false</code> otherwise
     */
    public boolean needsAmmunition(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState reference) {
        Preconditions.checkNotNull(reference, "reference must not be empty");

        if (!this.getWarShip().equals(reference.getWarShip())) {
            throw new NotifyWebUserException("The warship health states can only be checked for the same individual ships.");
        }

        final MissileAmmunitionState referenceMissiles = reference.getMissileAmmunitionState();

        return referenceMissiles.getRemainingShots().entrySet().stream().anyMatch(ref -> {
            final Missile missile = ref.getKey();
            final int refAmount = ref.getValue();
            final int remainingShots1 = getRemainingShots().get(missile);
            return refAmount != remainingShots1;
        });
    }

    public void repair(@Nonnull final Tick today) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final ShipClass shipClass = warShip.getShipClass();
        this.capabilities.clear();
        this.capabilities.addAll(new SpacecraftCalculator().getCapabilityValues(shipClass));
        this.activeFittings.clear();
        this.activeFittings.addAll(shipClass.getFittings());
        warShip.setOperational(today);
        warShip.animate();
        setFightingCapable(true);
        ammoUp();
    }

    public void ammoUp() {
        final ShipClass shipClass = warShip.getShipClass();
        final Map<Missile, Integer> shotsPerMissile = shipClass.getAmmunitionFittings().stream()
                .collect(Collectors.groupingBy(AmmunitionFitting::getMissile,
                        Collectors.mapping(AmmunitionFitting::getAmount, Collectors.reducing(0, Integer::sum))));

        this.remainingShots.clear();
        this.remainingShots.putAll(shotsPerMissile);
    }
}
