package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.util.List;

public interface TickRepository extends CrudRepository<Tick, Integer>, CustomTickRepository {

    @Nonnull
    @Query("SELECT t FROM Tick t ORDER BY t.id DESC")
    List<Tick> findLastTicks(final Pageable pageable);
}
