package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.MissionType;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue(MissionType.PIRATE_HUNT)
public class PirateHuntMission extends Mission {

    /**
     * The place to be.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPlanet")
    private Planet venue;

    public PirateHuntMission() {
    }

    public PirateHuntMission(@Nonnull final User actor, @Nonnull final Tick today, @Nonnull final Planet planet) {
        super(actor, today);

        this.venue = Preconditions.checkNotNull(planet, "planet must not be empty");
    }

    @Nonnull
    public Planet getVenue() {
        return venue;
    }
}
