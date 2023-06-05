package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class ResearchLevel {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this research.")
    private Research research;

    @JsonProperty
    @Schema(required = true, description = "The level of this research.")
    private int level;

    @Nullable
    @JsonProperty
    @Schema(description = "The amount of ticks.")
    private Integer ticksToComplete;

    public ResearchLevel() {
    }

    public ResearchLevel(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                         final int level,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.research = new Research(research, languageCode);
        this.level = level;
    }

    public ResearchLevel(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                         final int level,
                         final int remainingTicks,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.research = new Research(research, languageCode);
        this.level = level;
        this.ticksToComplete = remainingTicks;
    }

    public ResearchLevel(@Nonnull final de.yuga.spacebattle.backend.entities.researches.ResearchLevel researchLevel,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(researchLevel, "researchLevel shouldn't be null!");

        this.research = new Research(researchLevel.getResearch(), languageCode);
        this.level = researchLevel.getLevel();
    }

    @Nonnull
    public Research getResearch() {
        return research;
    }

    public int getLevel() {
        return level;
    }

    public void setTicksToComplete(@Nullable final Integer ticksToComplete) {
        this.ticksToComplete = ticksToComplete;
    }
}
