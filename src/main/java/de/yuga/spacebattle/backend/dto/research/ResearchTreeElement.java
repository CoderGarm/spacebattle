package de.yuga.spacebattle.backend.dto.research;


import javax.annotation.Nullable;

public class ResearchTreeElement {

    private int idResearch;

    private Integer idUnlockedBy;

    public ResearchTreeElement() {
    }

    public ResearchTreeElement(final int idResearch, @Nullable final Integer idUnlockedBy) {
        this.idResearch = idResearch;
        this.idUnlockedBy = idUnlockedBy;
    }

    public int getIdResearch() {
        return idResearch;
    }

    @Nullable
    public Integer getIdUnlockedBy() {
        return idUnlockedBy;
    }
}
