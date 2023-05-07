package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public interface ResearchLevelRepository extends CrudRepository<ResearchLevel, Integer>, CustomResearchLevelRepository {

    @Nullable
    @Query("SELECT l FROM ResearchLevel  l WHERE l.user.id = :idUser")
    Set<ResearchLevel> getAllForUser(@Param("idUser") final int idUser);

    /**
     * Checks is a user has the specific research already unlocked.
     *
     * @param idUser     the user id
     * @param idResearch the research id
     * @return <code>true</code> if the user already has this research unlocked, <code>false</code> otherwise
     */
    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM ResearchLevel r WHERE r.user.id = :idUser AND r.research.id = :idResearch AND r.level >= r.research.levelCap")
    boolean isResearchUnlocked(@Param("idUser") final int idUser, @Param("idResearch") final int idResearch);

    /**
     * Fetches the current level of a given research by this user.
     *
     * @param idUser     the user id
     * @param idResearch the research id
     * @return the current level or <code>null</code> if the research has no level
     */
    @Nullable
    @Query("SELECT r FROM ResearchLevel r WHERE r.user.id = :idUser AND r.research.id = :idResearch")
    ResearchLevel getResearchLevelFor(@Param("idUser") final int idUser, @Param("idResearch") final int idResearch);

    @Nullable
    @Query("SELECT r FROM ResearchLevel r WHERE r.user.id = :idUser AND r.research.id IN (:researchIds)")
    Set<ResearchLevel> getResearchLevelsFor(@Param("idUser") final int idUser, @Param("researchIds") final Collection<Integer> researchIds);
}
