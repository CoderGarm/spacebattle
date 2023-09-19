package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.researches.Research;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class TickAdvice {

    @JsonProperty
    @Schema(required = true, description = "If ground constructions are possible.")
    private boolean constructionPossible = false;

    @JsonProperty
    @Schema(required = true, description = "If shipyard jobs are possible.")
    private boolean shipyardPossible = false;

    @JsonProperty
    @Schema(required = true, description = "If a research is possible.")
    private boolean researchPossible = false;

    @JsonProperty
    @Schema(description = "The suggested building.")
    private Building suggestedBuilding;

    @JsonProperty
    @Schema(description = "The suggested research.")
    private Research suggestedResearch;

    public void setResearchPossible(final boolean researchPossible) {
        this.researchPossible = researchPossible;
    }

    public void setConstructionPossible(final boolean constructionPossible) {
        this.constructionPossible = constructionPossible;
    }

    public void setShipyardPossible(final boolean shipyardPossible) {
        this.shipyardPossible = shipyardPossible;
    }

    public void setSuggestedBuilding(final Building suggestedBuilding) {
        this.suggestedBuilding = suggestedBuilding;
    }

    public void setSuggestedResearch(final Research suggestedResearch) {
        this.suggestedResearch = suggestedResearch;
    }
}
