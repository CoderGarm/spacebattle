package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

@Schema(description = "The ids of a research by the id of the unlocking research and the id of an unlocked research.")
public class ResearchTreeElement {

    @JsonProperty
    @Schema(required = true, description = "The id of this research.")
    private int idResearch;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the research which unlocks this research.")
    private Integer idUnlockedBy;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the research which is unlocked by this research.")
    private Integer idUnlocks;

    public ResearchTreeElement(final int idResearch) {
        this.idResearch = idResearch;
    }

    @JsonIgnore
    public int getIdResearch() {
        return idResearch;
    }

    @Nullable
    @JsonIgnore
    public Integer getIdUnlockedBy() {
        return idUnlockedBy;
    }

    @Nullable
    public Integer getIdUnlocks() {
        return idUnlocks;
    }

    public void setIdUnlockedBy(@Nullable final Integer idUnlockedBy) {
        this.idUnlockedBy = idUnlockedBy;
    }

    public void setIdUnlocks(@Nullable final Integer idUnlocks) {
        this.idUnlocks = idUnlocks;
    }
}
