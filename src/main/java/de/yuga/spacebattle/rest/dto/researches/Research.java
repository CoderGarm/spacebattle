package de.yuga.spacebattle.rest.dto.researches;


import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Research {

    @Nonnull
    @Schema(required = true, description = "The id of this research.")
    private int idResearch;

    @Nonnull
    @Schema(required = true, description = "The name of this research.")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The description of this research.")
    private String description;

    @Schema(required = true, description = "The maximum level of this research.")
    private int levelCap;

    public Research() {
    }

    public Research(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.idResearch = research.getId();
        this.name = research.getName();
        this.description = research.getDescription();
        this.levelCap = research.getLevelCap();
    }

    public int getIdResearch() {
        return idResearch;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public int getLevelCap() {
        return levelCap;
    }
}
