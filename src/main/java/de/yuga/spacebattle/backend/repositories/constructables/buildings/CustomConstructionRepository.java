package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;

import javax.annotation.Nullable;

public interface CustomConstructionRepository {

    @Nullable
    ResourceDeposit getCosts(final int idBuilding);
}
