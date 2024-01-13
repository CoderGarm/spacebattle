package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Set;

@Service
public class MissionCategorizationService {

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public MissionCategorizationService(@Nonnull final PlanetService planetService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }

    public int getPlanetaryImpact(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        // todo I need a rework in season 3
        final Planet withConstructions = planetService.findWithConstructions(Set.of(planet)).get(0);
        return new UserPoints().withConstructions(withConstructions.getConstructions()).getPlanetaryPoints();
    }

}
