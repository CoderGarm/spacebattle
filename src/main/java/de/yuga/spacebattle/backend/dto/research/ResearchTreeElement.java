package de.yuga.spacebattle.backend.dto.research;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResearchTreeElement {

    private final Research research;

    public ResearchTreeElement(@Nonnull final Research research) {
        Preconditions.checkNotNull(research, "research must not be empty");

        this.research = research;
    }

    public int getIdResearch() {
        return research.getId();
    }

    @Nullable
    public Integer getIdUnlockedBy() {
        return research.getUnlockedThrough() != null ? research.getUnlockedThrough().getId() : null;
    }

    public Research getResearch() {
        return research;
    }
}
