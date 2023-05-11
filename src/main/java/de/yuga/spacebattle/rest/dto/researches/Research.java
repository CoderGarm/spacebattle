package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Research {

    @JsonProperty
    @Schema(required = true, description = "The id of this research.")
    private int idResearch;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this research.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this research.")
    private String description;

    @JsonProperty
    @Schema(required = true, description = "The maximum level of this research.")
    private int levelCap;

    @Nullable
    @JsonProperty
    @Schema(description = "If it has an icon, then it is described here.")
    private HasIcon hasIcon;

    public Research() {
    }

    public Research(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                    @Nonnull final String name,
                    @Nonnull final String description) {
        Preconditions.checkNotNull(research, "research must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");

        this.idResearch = research.getId();
        this.name = name;
        this.description = description;
        this.levelCap = research.getLevelCap();
        setIcon(research);
    }

    public Research(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                    @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.idResearch = research.getId();
        this.name = research.getName(languageCode);
        this.description = research.getDescription(languageCode);
        this.levelCap = research.getLevelCap();
        setIcon(research);
    }

    @JsonIgnore
    private void setIcon(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research) {
        Preconditions.checkNotNull(research, "research must not be empty");

        final EResourceType eResourceType = research.getUnlocksBuildings().stream().findFirst().map(Building::getProductionTarget).orElse(null);
        if (eResourceType != null) {
            this.hasIcon = new HasIcon(eResourceType);
            return;
        }
        this.hasIcon = HasIcon.getBy(research.getUnlocks());
    }

    public int getIdResearch() {
        return idResearch;
    }

    @Nonnull
    public String getName() {
        return name;
    }
}
