package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "ElectronicWarfare.getAll", query = "SELECT a FROM ElectronicWarfare a"),
        @NamedQuery(name = "ElectronicWarfare.getAllByResearches", query = "SELECT a FROM ElectronicWarfare a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "electronicWarfare")
@AttributeOverride(name = "id", column = @Column(name = "idElectronicWarfare"))
public class ElectronicWarfare extends BaseModuleWithEffectValue {

    /**
     * Defines the range of this eloka in meter.
     */
    @Column(nullable = false)
    private int effectiveRange;

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final String name,
                             @Nonnull final String description,
                             @Nonnull final Research unlockedThrough,
                             final int useCapacity,
                             final int effectValue,
                             final int effectiveRange,
                             final int techLevel,
                             @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement);

        this.effectiveRange = effectiveRange;
    }

    public int getEffectiveRange() {
        return effectiveRange;
    }
}
