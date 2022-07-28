package de.yuga.spacebattle.rest.dto.turn.resources;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.enums.EDepositType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ResourceDeposit {

    /**
     * The amount of resources and their type.<br>
     * <b>Attention:</b> The {@link EResourceType#POPULATION} is something special.
     */
    @Nonnull
    @Schema(required = true, description = "The amount of stored resources by their type.")
    private List<ResourceAmount> resources;

    @Nonnull
    @Schema(required = true, description = "The amount of human resources by their type.")
    private List<HumanResourceAmount> humanResources;

    @Nonnull
    @Schema(required = true, description = "The type of the deposit - costs or a real deposit.")
    private EDepositType subType;

    public ResourceDeposit() {
    }

    public ResourceDeposit(@Nonnull final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        this.subType = new EDepositType(resourceDeposit.getSubType());

        this.resources = Arrays.stream(EResourceType.values()).map(eResourceType -> {
            final long resourceAmountByType = resourceDeposit.getResourceAmountByType(eResourceType);
            return new ResourceAmount(eResourceType, resourceAmountByType);
        }).collect(Collectors.toList());

        this.humanResources = Arrays.stream(EEducationType.values()).map(eEducationType -> {
            final long resourceAmountByType = resourceDeposit.getCrewAmountByType(eEducationType);
            return new HumanResourceAmount(eEducationType, resourceAmountByType);
        }).collect(Collectors.toList());
    }

    @Nonnull
    public List<ResourceAmount> getResources() {
        return resources;
    }

    @Nonnull
    public List<HumanResourceAmount> getHumanResources() {
        return humanResources;
    }

    @Nonnull
    public EDepositType getSubType() {
        return subType;
    }
}
