package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Colonization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface ColonizationRepository extends CrudRepository<Colonization, Integer>, CustomColonizationRepository {

    @Nullable
    @Query("SELECT c FROM Colonization c WHERE c.target.system.id = :idStarSystem")
    List<Colonization> findAllForSystem(@Param("idStarSystem") final int idStarSystem);

    @Nullable
    @Query("SELECT c FROM Colonization c WHERE c.user.id = :idUser AND c.isPlanned = true")
    List<Colonization> findAllPlannedForUser(@Param("idUser") final int idUser);
}
