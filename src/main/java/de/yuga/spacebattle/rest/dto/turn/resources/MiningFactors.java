package de.yuga.spacebattle.rest.dto.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class MiningFactors {

    @Nonnull
    @Schema(required = true, description = "The factor by resource type.")
    private List<ResourceAmount> resources;

    public MiningFactors() {
    }

    public MiningFactors(@Nonnull final de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors miningFactors) {
        Preconditions.checkNotNull(miningFactors, "miningFactors shouldn't be null!");

        this.resources = Arrays.stream(EResourceType.values()).map(eResourceType -> {
            final double resourceAmountByType = miningFactors.getMiningFactorByType(eResourceType);
            return new ResourceAmount(eResourceType, (long) (resourceAmountByType * 100));
        }).collect(Collectors.toList());
    }

    @Nonnull
    public List<ResourceAmount> getResources() {
        return resources;
    }
}
