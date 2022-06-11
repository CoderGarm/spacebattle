package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.researches.ActiveResearchTuple;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomJobRepository {

    @Nonnull
    List<Job> findAllJobs();

    @Nonnull
    List<Job> findAllJobsForConstruction(@Nonnull final Construction facility);

    @Nonnull
    List<Job> findAllJobsByPlanet(final int idPlanet);

    boolean isJobActiveFor(@Nonnull Research research);

    @Nullable
    List<ActiveResearchTuple> isJobActiveFor(@Nonnull List<Research> researches);
}
