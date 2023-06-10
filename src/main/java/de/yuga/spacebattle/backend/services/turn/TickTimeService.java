package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service /* todo better distinguish between tick-worker-service and tick-time-service */
public class TickTimeService {

    @Nonnull
    private final TickRepository tickRepository;

    @Autowired
    public TickTimeService(@Nonnull final TickRepository tickRepository) {
        this.tickRepository = Preconditions.checkNotNull(tickRepository, "tickRepository must not be empty");
    }

    @Nonnull
    @SuppressWarnings("DataFlowIssue")
    public Tick getToday() {
        return tickRepository.getLatest();
    }

    public List<Tick> getTimeframe(final int pastTicks) {
        return tickRepository.findLastTicks(PageRequest.of(0, pastTicks));
    }
}
