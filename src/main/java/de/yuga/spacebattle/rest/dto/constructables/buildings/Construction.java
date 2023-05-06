package de.yuga.spacebattle.rest.dto.constructables.buildings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Construction {

    /**
     * If this is null, the information about the entity must be fetched.
     */
    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The ID of this construction.")
    private Integer idConstruction;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The building which is the base of this construction.")
    private Building building;

    @JsonProperty
    @Schema(required = true, description = "The level of this construction.")
    private int level;

    @JsonProperty
    @Schema(required = true, description = "If there is a next level.")
    private boolean nextLevel = false;

    @JsonProperty
    @Schema(required = true, description = "The active level of this construction.")
    private int operationalLevel;

    public Construction() {
    }

    public Construction(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.buildings.Construction construction,
                        @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        this.idConstruction = construction.getId();
        this.building = new Building(construction.getBuilding(), languageCode);
        this.level = construction.getLevel();
        this.operationalLevel = construction.getOperationalLevel();
    }

    @JsonIgnore
    public void activateNextLevel() {
        this.nextLevel = true;
    }
}
