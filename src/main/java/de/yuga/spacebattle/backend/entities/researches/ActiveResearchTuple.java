package de.yuga.spacebattle.backend.entities.researches;

import javax.annotation.Nonnull;

public class ActiveResearchTuple {

    @Nonnull
    private final Research research;

    private final boolean activeJob;

    public ActiveResearchTuple(@Nonnull final Research research, final boolean activeJob) {
        this.research = research;
        this.activeJob = activeJob;
    }

    @Nonnull
    public Research getResearch() {
        return research;
    }

    public boolean isActiveJob() {
        return activeJob;
    }
}
