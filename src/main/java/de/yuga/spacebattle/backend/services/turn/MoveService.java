package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.repositories.turn.MoveRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MoveService {

    @Nonnull
    private final MoveRepository moveRepository;

    public MoveService(@Nonnull final MoveRepository moveRepository) {
        Preconditions.checkNotNull(moveRepository, "moveRepository shouldn't be null!");

        this.moveRepository = moveRepository;
    }

    @Nonnull
    public List<Move> findAllUncompleted() {
        return Objects.requireNonNullElse(moveRepository.findAllUncompleted(), new ArrayList<>());
    }

    @Nullable
    public Move find(@Nonnull final Integer idMove) {
        Preconditions.checkNotNull(idMove, "idMove shouldn't be null!");
        return moveRepository.findById(idMove).orElse(null);
    }

    public Move save(@Nonnull final Move entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return moveRepository.save(entity);
    }

    @Nonnull
    public List<Move> findFinishedInSystems(@Nonnull final Tick today, @Nonnull final Set<Integer> systemIDs) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(systemIDs, "systemIDs must not be empty");

        final List<Move> moves = Objects.requireNonNullElse(moveRepository.findFinishedInSystems(today.getNo(), systemIDs), new ArrayList<>());
        final Map<FleetSnapshot, List<Move>> byFleet = moves.stream()
                .filter(m -> Objects.nonNull(m.getFleetSnapshot()))
                .collect(Collectors.groupingBy(Move::getFleetSnapshot,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        moves.removeIf(m -> {
            final FleetSnapshot fleetSnapshot = m.getFleetSnapshot();
            final List<Move> sortedMovesByAgeReverse = byFleet.get(fleetSnapshot).stream().sorted((o1, o2) -> Integer.compare(o2.getId(), o1.getId())).collect(Collectors.toList());
            if (sortedMovesByAgeReverse.size() > 1) {
                return m.equals(sortedMovesByAgeReverse.get(0));
            }
            return false;
        });

        return moves;
    }

    @Nonnull
    public List<Move> saveAll(@Nonnull final Collection<Move> moves) {
        Preconditions.checkNotNull(moves, "moves must not be empty");

        return moveRepository.saveAll(moves);
    }

    public List<Move> forDeletionFindAllByOwner(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return Objects.requireNonNullElse(moveRepository.forDeletionFindAllByOwner(user), new ArrayList<>());
    }
}
