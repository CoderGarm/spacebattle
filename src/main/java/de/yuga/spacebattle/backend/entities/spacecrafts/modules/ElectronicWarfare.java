package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.ETechLevel;

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
    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance effectiveRange;

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final String name,
                             @Nonnull final String description,
                             @Nonnull final Research unlockedThrough,
                             final int useCapacity,
                             final int effectValue,
                             @Nonnull final Distance effectiveRange,
                             @Nonnull final ETechLevel techLevel,
                             @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement);
        Preconditions.checkNotNull(effectiveRange, "effectiveRange shouldn't be null!");

        this.effectiveRange = effectiveRange;
    }

    public Distance getEffectiveRange() {
        return effectiveRange;
    }
}
