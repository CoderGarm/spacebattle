package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.enums.EShipClassType;
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

    @JsonProperty
    @Schema(description = "The suggested class to build up.")
    private EShipClassType suggestedShipClass;

    @JsonProperty
    @Schema(description = "The suggested class to build up.")
    private EMissionType suggestedMission;

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

    public void setSuggestedShipClass(final de.yuga.spacebattle.backend.enums.EShipClassType suggestedShipClass) {
        this.suggestedShipClass = new EShipClassType(suggestedShipClass);
    }

    public void setSuggestedMission(final EMissionType suggestedMission) {
        this.suggestedMission = suggestedMission;
    }

    public boolean isConstructionPossible() {
        return constructionPossible;
    }

    public boolean isShipyardPossible() {
        return shipyardPossible;
    }

    public boolean isResearchPossible() {
        return researchPossible;
    }

    public Building getSuggestedBuilding() {
        return suggestedBuilding;
    }

    public Research getSuggestedResearch() {
        return suggestedResearch;
    }

    public EShipClassType getSuggestedShipClass() {
        return suggestedShipClass;
    }

    public EMissionType getSuggestedMission() {
        return suggestedMission;
    }
}
