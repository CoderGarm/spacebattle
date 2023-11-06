package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Move;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface MoveRepository extends CrudRepository<Move, Integer> {

    @Nullable
    @Query("SELECT m FROM Move m WHERE m.isDeleted = false")
    List<Move> findAllUncompleted();

    @Nullable
    @Query("SELECT DISTINCT m FROM Move m WHERE m.finished.id = :today AND m.destinationOrbit.system.id IN (:systemIDs)")
    List<Move> findFinishedInSystems(final int today, @Nonnull final Set<Integer> systemIDs);
}
