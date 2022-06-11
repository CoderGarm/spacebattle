package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.Set;

public interface ResearchLevelRepository extends CrudRepository<ResearchLevel, Integer>, CustomResearchLevelRepository {

    @Nullable
    @Query("SELECT l FROM ResearchLevel  l WHERE l.user.id = :idUser")
    Set<ResearchLevel> getAllForUser(@Param("idUser") final int idUser);
}
