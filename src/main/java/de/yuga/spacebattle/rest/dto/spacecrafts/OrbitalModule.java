package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.enums.EModuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Size;

@Schema(description = ".")
public class OrbitalModule extends WithCosts<OrbitalModule> {

    @Nullable
    @JsonProperty
    @Schema(description = "The ID.")
    private Integer idOrbitalModule;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this class.")
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The tactical usage of this module.")
    protected EModuleType moduleType;

    @JsonProperty
    @Schema(required = true, description = "The effect value of this module.")
    protected int effectValue;

    public OrbitalModule() {
    }

    public OrbitalModule(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule orbitalModule,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(orbitalModule, "orbitalModule shouldn't be null!");

        this.idOrbitalModule = orbitalModule.getId();
        this.name = orbitalModule.getName(languageCode);
        this.moduleType = new EModuleType(orbitalModule.getEffect());
        this.effectValue = orbitalModule.getBaseValue();

        /* fixme add parameter and also on other modules! */
    }
}
