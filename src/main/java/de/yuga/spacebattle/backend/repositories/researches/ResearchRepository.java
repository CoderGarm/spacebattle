package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nullable;
import java.util.List;

public interface ResearchRepository extends CrudRepository<Research, Integer>, CustomResearchRepository {

    @Nullable
    @Query("SELECT r FROM Research r WHERE r.unlockedThrough IS NULL")
    List<Research> getResearchesWithoutPrecondition();
}
