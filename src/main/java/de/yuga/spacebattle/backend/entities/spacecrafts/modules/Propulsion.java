package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * There will be only one propulsion type and it will be taken for FTL- and sub light-travelling.
 * The propulsion is dual-use for both in every ship.
 */
@NamedQueries({
        @NamedQuery(name = "Propulsion.getAll", query = "SELECT a FROM Propulsion a"),
        @NamedQuery(name = "Propulsion.getAllByResearches", query = "SELECT a FROM Propulsion a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "propulsion")
@AttributeOverride(name = "id", column = @Column(name = "idPropulsion"))
public class Propulsion extends BaseModuleWithEffectValue {

    /**
     * If this propulsion module provides the ability to travel faster than light.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHyperBand hyperBand;

    /**
     * If this propulsion module is for military or civil purposes.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechnologyType technologyType;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final String name,
                      @Nonnull final String description,
                      @Nonnull final Research unlockedThrough,
                      final int useCapacity,
                      final int effectValue,
                      @Nonnull final EHullType hullType,
                      @Nonnull final ETechLevel techLevel,
                      @Nonnull final EHyperBand hyperBand,
                      @Nonnull final ETechnologyType technologyType,
                      @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, hullType, techLevel, crewRequirement, Propulsion.class);
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");

        this.hyperBand = hyperBand;
        this.technologyType = technologyType;
    }

    @Nonnull
    public EHyperBand getHyperBand() {
        return hyperBand;
    }

    @Nonnull
    public ETechnologyType getTechnologyType() {
        return technologyType;
    }

    public boolean isFtlCapable() {
        return hyperBand != EHyperBand.NONE;
    }
}
