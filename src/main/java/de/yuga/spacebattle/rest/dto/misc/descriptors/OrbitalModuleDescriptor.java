package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class OrbitalModuleDescriptor {

    @Nullable
    @JsonProperty
    @Schema(description = "The amount of inhabitants.")
    private Integer inhabitants;

    @Nullable
    @JsonProperty
    @Schema(description = "The increasement for the pop factor.")
    private Integer popFactorIncreasement;

    public OrbitalModuleDescriptor() {
    }

    public OrbitalModuleDescriptor(@Nonnull final OrbitalModule content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        if (content.isHabitat()) {
            this.inhabitants = content.getInhabitants();
            this.popFactorIncreasement = content.getPopFactorIncreasement();
        }
    }
}
