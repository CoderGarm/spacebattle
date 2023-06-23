package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomConstructionRepository {

    List<Construction> findAllConstructions();

    @Nonnull
    List<Construction> findAllConstructionsOnPlanet(int idPlanet);
}
