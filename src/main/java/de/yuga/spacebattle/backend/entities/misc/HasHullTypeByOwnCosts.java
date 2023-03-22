package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EResourceDemand;

import javax.annotation.Nonnull;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.util.Map;

@MappedSuperclass
public class HasHullTypeByOwnCosts extends HasCostsByOwn {

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    public HasHullTypeByOwnCosts() {
    }

    public HasHullTypeByOwnCosts(@Nonnull final NamedTechLevel baseModule,
                                 @Nonnull final String technicalTypeName,
                                 final int unlockedThroughLevel,
                                 final int effectValue,
                                 final int useCapacity,
                                 @Nonnull final EHullType hullType) {
        this(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, useCapacity, hullType, new CrewRequirement(Map.of(), EDepositType.COSTS));
    }

    public HasHullTypeByOwnCosts(@Nonnull final NamedTechLevel baseModule,
                                 @Nonnull final String technicalTypeName,
                                 final int unlockedThroughLevel,
                                 final int effectValue,
                                 final int useCapacity,
                                 @Nonnull final EHullType hullType,
                                 @Nonnull final CrewRequirement crewRequirement) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, useCapacity, crewRequirement);

        this.hullType = Preconditions.checkNotNull(hullType, "hullType must not be empty");
    }

    public HasHullTypeByOwnCosts(@Nonnull final NamedTechLevel baseModule,
                                 @Nonnull final String technicalTypeName,
                                 final int unlockedThroughLevel,
                                 final int useCapacity,
                                 @Nonnull final EHullType hullType,
                                 @Nonnull final CrewRequirement crewRequirement,
                                 @Nonnull final EResourceDemand eResourceDemand) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, 0, useCapacity, crewRequirement, eResourceDemand);

        this.hullType = Preconditions.checkNotNull(hullType, "hullType must not be empty");
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }
}
