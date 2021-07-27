package de.yuga.spacebattle.rest.dto.researches;


import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class ResearchLevel {

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this research.")
    private Research research;

    @ApiModelProperty(required = true, value = "The level of this research.")
    private int level;

    public ResearchLevel() {
    }

    public ResearchLevel(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research, final int level) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.research = new Research(research);
        this.level = level;
    }

    @Nonnull
    public Research getResearch() {
        return research;
    }

    public int getLevel() {
        return level;
    }
}
