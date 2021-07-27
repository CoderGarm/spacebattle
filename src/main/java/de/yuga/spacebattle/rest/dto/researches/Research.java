package de.yuga.spacebattle.rest.dto.researches;


import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Research {

    @Nonnull
    @ApiModelProperty(required = true, value = "The id of this research.")
    private int idResearch;

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this research.")
    private String name;

    @Nonnull
    @ApiModelProperty(required = true, value = "The description of this research.")
    private String description;

    @ApiModelProperty(required = true, value = "The maximum level of this research.")
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
