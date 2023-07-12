package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.MissionType;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MissionType.PIRATE_HUNT)
public class PirateHuntMission extends Mission {

    public PirateHuntMission() {
    }

    public PirateHuntMission(@Nonnull final User actor, @Nonnull final Tick today, @Nonnull final Planet planet) {
        super(actor, today, planet);
    }
}
