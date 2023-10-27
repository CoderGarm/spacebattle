package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Job;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomJobRepository {

    @Nonnull
    List<Job> findAllJobsByPlanet(final int idPlanet);

    @Nonnull
    List<Job> findAllJobsForUser(final int idUser);
}
