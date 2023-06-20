package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.converter.MassConverter;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.misc.HasEffectValue;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import jakarta.persistence.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "PassiveModule.getAll", query = "SELECT a FROM PassiveModule a"),
        @NamedQuery(name = "PassiveModule.getAllByResearches",
                query = "SELECT a FROM PassiveModule a LEFT JOIN ResearchLevel rl ON (rl.research = a.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "passiveModule")
@AttributeOverride(name = "id", column = @Column(name = "idPassiveModule"))
public class PassiveModule extends HasCosts implements HasEffectValue {

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    private int unlockedThroughLevel;

    /**
     * The capacity represents the capacity in metric tons which will be occupied if build in.<br>
     * This capacity includes all 'opportunity costs' to use a module, from crew quarters up to toilets, from screens and displays up to seats and impact cages.
     */
    @NotNull
    @Nonnull
    @Convert(converter = MassConverter.class)
    private Mass tonnage;

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EShipClassType shipClassType;

    /**
     * Defines what kind of property is supported.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ESupportType supportType;

    /**
     * Defines if the support is increasing or decreasing the property.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ECalculationType calculationType;

    private int effectValue;

    public PassiveModule() {
    }

    public PassiveModule(@Nonnull final String name,
                         @Nonnull final String description,
                         @Nonnull final Research unlockedThrough,
                         final int unlockedThroughLevel,
                         final int tonnage,
                         final int effectValue,
                         @Nonnull final EShipClassType shipClassType,
                         @Nonnull final ETechLevel techLevel,
                         @Nonnull final ESupportType supportType,
                         @Nonnull final ECalculationType calculationType,
                         @Nonnull final CrewRequirement crewRequirement) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, tonnage, PassiveModule.class);

        this.supportType = supportType;
        this.calculationType = calculationType;
        this.effectValue = effectValue;
        this.unlockedThrough = unlockedThrough;
        this.unlockedThroughLevel = unlockedThroughLevel;
        this.tonnage = new Mass(tonnage, EMassMetric.T);
        this.shipClassType = shipClassType;
        this.getCosts().setCrewRequirement(crewRequirement);
    }

    @Nonnull
    public ESupportType getSupportType() {
        return supportType;
    }

    public boolean isPassenger() {
        return supportType == ESupportType.PASSENGER;
    }

    public int getPassengers() {
        if (!isPassenger()) {
            return 0;
        }
        return getEffectValue();
    }

    public boolean isCargo() {
        return supportType == ESupportType.FREIGHT;
    }

    @Nonnull
    public Mass getCargoCapacity() {
        if (!isCargo()) {
            return Mass.ZERO;
        }
        return new Mass(getEffectValue(), EMassMetric.T);
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    public int getUnlockedThroughLevel() {
        return unlockedThroughLevel;
    }

    @Nonnull
    public Mass getTonnage() {
        return tonnage;
    }

    @Nonnull
    public EShipClassType getShipClassType() {
        return shipClassType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setShipClassType(@Nonnull final EShipClassType shipClassType) {
        this.shipClassType = shipClassType;
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }
}
