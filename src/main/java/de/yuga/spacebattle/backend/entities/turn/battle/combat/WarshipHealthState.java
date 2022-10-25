package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.FittingUtils;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "warshipHealthState")
@AttributeOverride(name = "id", column = @Column(name = "idWarshipHealthState"))
public class WarshipHealthState extends AbstractEntityKey {

    /**
     * The war ship which is this state for.
     */
    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idWarship")
    @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
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

    @Column(columnDefinition = "bit not null default true")
    private boolean isFightingCapable = true;

    public WarshipHealthState() {
    }

    public WarshipHealthState(@Nonnull final WarShip warship) {
        Preconditions.checkNotNull(warship, "warship must not be empty");

        final ShipClass shipClass = warship.getShipClass();
        this.warShip = warship;
        this.capabilities.addAll(new SpacecraftCalculator().getCapabilityValues(shipClass));
        this.activeFittings.addAll(shipClass.getFittings());
        final Set<AlignedFitting> fittingByType = this.activeFittings.stream()
                .filter(FittingUtils.MISSILES)
                .collect(Collectors.toSet());

        final Map<Missile, Integer> shotsPerMissile = fittingByType.stream()
                .map(AlignedFitting::getLauncher)
                .filter(Objects::nonNull)
                .map(Launcher::getAmmunitionModule)
                .collect(Collectors.groupingBy(AmmunitionModule::getMissile,
                        Collectors.mapping(AmmunitionModule::getEffectValue, Collectors.reducing(0, Integer::sum))));

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
        this.activeFittings.clear();
        this.activeFittings.addAll(warshipHealthState.getActiveFittings());
        this.remainingShots.clear();
        this.remainingShots.putAll(warshipHealthState.getMissileAmmunitionState().getRemainingShots());
    }

    public double getStateByAsDouble(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().doubleValue()).reduce(0D, Double::sum);
    }

    public int getStateByAsInt(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType must not be empty");

        return capabilities.stream().filter(cap -> cap.getModuleType() == eModuleType).map(cap -> cap.getValue().intValue()).reduce(0, Integer::sum);
    }

    @Nonnull
    public WarShip getWarShip() {
        return warShip;
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

    /**
     * States if the war ship is alive.
     *
     * @return <code>true</code> if the ship can is probably a hulk, but not destroyed, <code>false</code> otherwise
     */
    public boolean isAlive() {
        return warShip.isAlive();
    }

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
    public boolean hasChanged(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState reference) {
        Preconditions.checkNotNull(reference, "reference must not be empty");

        if (!this.getWarShip().equals(reference.getWarShip())) {
            throw new NotifyWebUserException("The warship health states can only be checked for the same individual ships.");
        }

        final boolean differState = !(getStateByAsInt(EModuleType.ARMOR) == reference.getArmorState()
                && getStateByAsInt(EModuleType.ELECTRONIC_WARFARE) == reference.getElokaState()
                && getStateByAsInt(EModuleType.SHIELD) == reference.getSidewallState()
                && getStateByAsInt(EModuleType.ARMOR) == reference.getHullState()
                && getStateByAsInt(EModuleType.PROPULSION) == reference.getPropulsionState());

        final MissileAmmunitionState referenceMissiles = reference.getMissileAmmunitionState();
        final boolean differMissiles = referenceMissiles.getRemainingShots().entrySet().stream().anyMatch(ref -> {
            final Missile missile = ref.getKey();
            final int refAmount = ref.getValue();
            final int remainingShots = getRemainingShots().get(missile);
            return refAmount != remainingShots;
        });

        return differState || differMissiles;
    }
}
