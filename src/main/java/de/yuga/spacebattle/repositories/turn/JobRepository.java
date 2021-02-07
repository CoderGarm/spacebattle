package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.turn.Job;
import org.springframework.data.repository.CrudRepository;

public interface JobRepository extends CrudRepository<Job, Integer>, CustomJobRepository {
}
