package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import java.util.List;

public interface TickRepository extends JpaRepository<Tick, Integer>, CustomTickRepository {

    @Nonnull
    @Query("SELECT t FROM Tick t ORDER BY t.id DESC")
    List<Tick> findLastTicks(final Pageable pageable);
}
