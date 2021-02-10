package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.repositories.turn.MoveRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class MoveService {

    @Nonnull
    private final MoveRepository moveRepository;

    public MoveService(@Nonnull final MoveRepository moveRepository) {
        Preconditions.checkNotNull(moveRepository, "moveRepository shouldn't be null!");

        this.moveRepository = moveRepository;
    }

    @Nonnull
    public List<Move> findAll() {
        return moveRepository.findAllMoves();
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

    public void delete(@Nonnull final Move entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        moveRepository.delete(entity);
    }
}
