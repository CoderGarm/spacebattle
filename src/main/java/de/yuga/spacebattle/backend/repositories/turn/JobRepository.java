package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends CrudRepository<Job, Integer>, CustomJobRepository {

    @Query("SELECT p.constructable.research FROM Job p WHERE p.constructable.research IS NOT NULL AND p.owner.id = :idUser")
    List<Research> getResearchesFromActiveJobs(@Param("idUser") final int idUser);
}
