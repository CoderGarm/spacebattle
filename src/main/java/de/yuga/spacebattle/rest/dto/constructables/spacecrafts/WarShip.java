package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import de.yuga.spacebattle.rest.dto.turn.battle.WarshipHealthState;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class WarShip {

    @JsonProperty
    @Schema(required = true, description = "The id of the ship.")
    private int idWarship;

    @Nonnull
    @Schema(required = true, description = "The name of this individual ship.")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The birthplace of this ship.")
    private Planet shipyard;

    @Nonnull
    @Schema(required = true, description = "The fleet which this ship is part of.")
    private int idFleet;

    @Nonnull
    @Schema(required = true, description = "The ship class which this ship is a type of.")
    private ShipClass shipClass;

    @Nonnull
    @JsonProperty
    @Schema(description = "The ship class which this ship is a type of.")
    private WarshipHealthState warshipHealthState;

    public WarShip() {
    }

    public WarShip(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip,
                   @Nullable final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState healthState,
                   @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        this.idWarship = warShip.getId();
        this.name = warShip.getName();
        this.shipyard = new de.yuga.spacebattle.rest.dto.orbitals.Planet(warShip.getShipyard());
        this.idFleet = warShip.getFleet().getId();
        this.shipClass = new de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass(warShip.getShipClass(), languageCode);
        if (healthState != null) {
            this.warshipHealthState = new WarshipHealthState(healthState);
        }
    }

    public int getIdWarship() {
        return idWarship;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Planet getShipyard() {
        return shipyard;
    }

    public int getIdFleet() {
        return idFleet;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }
}
