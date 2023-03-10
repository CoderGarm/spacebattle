package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByParent;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
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
public class Propulsion extends HasCostsByParent {

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

    public Propulsion(@Nonnull final NamedTechLevel baseModule,
                      @Nonnull final String technicalTypeName,
                      @Nonnull final Research unlockedThrough,
                      final int effectValue,
                      final int costsPercentage,
                      @Nonnull final EHyperBand hyperBand,
                      @Nonnull final ETechnologyType technologyType) {
        super(baseModule, technicalTypeName, unlockedThrough, costsPercentage, effectValue);
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

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
