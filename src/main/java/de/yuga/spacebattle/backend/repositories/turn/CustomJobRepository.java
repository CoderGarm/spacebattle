package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Job;

import java.util.List;

public interface CustomJobRepository {

    List<Job> findAllJobs();

    boolean researchPossible(final User user);
}
