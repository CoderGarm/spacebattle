package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomConstructionRepository {

    List<Construction> findAllConstructions();

    @Nonnull
    List<Construction> findAllConstructionsOnPlanet(int idPlanet);

    @Nullable
    ResourceDeposit getCosts(final int idBuilding);
}
