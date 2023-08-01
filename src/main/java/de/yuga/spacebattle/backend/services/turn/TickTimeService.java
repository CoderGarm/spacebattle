package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class TickTimeService {

    @Nonnull
    private final TickRepository tickRepository;

    @Autowired
    public TickTimeService(@Nonnull final TickRepository tickRepository) {
        this.tickRepository = Preconditions.checkNotNull(tickRepository, "tickRepository must not be empty");
    }

    @Nonnull
    public Tick getToday() {
        final Tick latest = tickRepository.getLatest();
        // most edgy edge case on a fresh database setup - the tick will be created while startup but cache loading happens before
        return Objects.requireNonNullElseGet(latest, Tick::new);
    }

    @Nonnull
    public List<Tick> getTimeframe(final int pastTicks) {
        return tickRepository.findLastTicks(PageRequest.of(0, pastTicks));
    }

    @Nonnull
    public List<Tick> findAll() {
        return tickRepository.findAllTicks();
    }

    @Nonnull
    public List<Tick> findAll(@Nonnull final Collection<Integer> idTicks) {
        Preconditions.checkNotNull(idTicks, "idTicks must not be empty");

        return Objects.requireNonNullElse(tickRepository.findAllById(idTicks), new ArrayList<>());
    }

    @Nullable
    public Tick find(@Nonnull final Integer idTick) {
        Preconditions.checkNotNull(idTick, "idTick shouldn't be null!");

        return tickRepository.findById(idTick).orElse(null);
    }
}
