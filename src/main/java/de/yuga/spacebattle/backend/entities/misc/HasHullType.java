package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EHullType;

import javax.annotation.Nonnull;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class HasHullType extends HasCostsByParent {

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    public HasHullType() {
    }

    public HasHullType(@Nonnull final NamedTechLevel baseModule,
                       @Nonnull final String technicalTypeName,
                       @Nonnull final Research unlockedThrough,
                       final int costsPercentage,
                       final int effectValue,
                       @Nonnull final EHullType hullType) {
        super(baseModule, technicalTypeName, unlockedThrough, costsPercentage, effectValue);

        this.hullType = Preconditions.checkNotNull(hullType, "hullType must not be empty");
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }
}
