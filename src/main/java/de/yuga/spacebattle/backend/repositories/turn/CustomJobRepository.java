package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Job;

import java.util.List;

public interface CustomJobRepository {

    List<Job> findAllJobs();
}
