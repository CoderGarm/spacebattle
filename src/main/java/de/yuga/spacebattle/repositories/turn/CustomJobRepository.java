package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.turn.Job;

import java.util.List;

public interface CustomJobRepository {

    List<Job> findAllJobs();

    boolean researchPossible(final User user);
}
