package de.yuga.spacebattle.backend.services.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.repositories.turn.battle.combat.WarshipHealthStateRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class WarshipHealthStateService {

    @Nonnull
    private final WarshipHealthStateRepository warshipHealthStateRepository;

    public WarshipHealthStateService(@Nonnull final WarshipHealthStateRepository warshipHealthStateRepository) {
        this.warshipHealthStateRepository = Preconditions.checkNotNull(warshipHealthStateRepository, "warshipHealthStateRepository must not be empty");
    }

    @Nonnull
    public List<WarshipHealthState> findByWarships(@Nonnull final List<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips must not be empty");

        final List<WarshipHealthState> byWarships = warshipHealthStateRepository.findByWarships(warShips);
        return Objects.requireNonNullElse(byWarships, new ArrayList<>());
    }

    public void saveAll(@Nonnull final Collection<WarshipHealthState> warshipHealthStates) {
        Preconditions.checkNotNull(warshipHealthStates, "warshipHealthStates must not be empty");

        warshipHealthStateRepository.saveAll(warshipHealthStates);
    }

    public void save(@Nonnull final WarshipHealthState healthState) {
        Preconditions.checkNotNull(healthState, "healthState must not be empty");

        warshipHealthStateRepository.save(healthState);
    }
}
