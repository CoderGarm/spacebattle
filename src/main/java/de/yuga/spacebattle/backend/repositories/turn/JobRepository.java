package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Job;
import org.springframework.data.repository.CrudRepository;

public interface JobRepository extends CrudRepository<Job, Integer>, CustomJobRepository {
}
