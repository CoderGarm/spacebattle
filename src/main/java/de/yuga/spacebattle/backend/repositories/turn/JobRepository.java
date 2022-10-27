package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface JobRepository extends CrudRepository<Job, Integer>, CustomJobRepository {

    @Nullable
    @Query("SELECT p.constructable.research FROM Job p WHERE p.constructable.research IS NOT NULL AND p.owner.id = :idUser")
    List<Research> getResearchesFromActiveJobs(@Param("idUser") final int idUser);

    @Query("SELECT CASE WHEN (COUNT(j) > 0) THEN TRUE ELSE FALSE END FROM Job j WHERE j.constructable.fleet.id = :idFleet AND j.constructable.fleet.owner.id = :idUser")
    boolean isJobRunningFor(@Param("idUser") final int idUser, @Param("idFleet") final int idFleet);

    @Nullable
    @Query("SELECT j FROM Job j WHERE j.owner.id = :idUser AND j.finished.id = (SELECT MAX(today.id) FROM Tick today)")
    List<Job> findTodayFinishedJobsForUser(@Param("idUser") final int idUser);

    @Query("SELECT CASE WHEN (COUNT(j) > 0) THEN TRUE ELSE FALSE END  FROM Job j WHERE j.owner.id = :idUser AND j.finished.id = (SELECT MAX(today.id) FROM Tick today)")
    boolean areTodayFinishedJobsForUserPresent(@Param("idUser") final int idUser);
}
