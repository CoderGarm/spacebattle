package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import de.yuga.spacebattle.rest.dto.turn.battle.combat.WarshipHealthState;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class WarShip {

    @JsonProperty
    @Schema(required = true, description = "The id of the ship.")
    private int idWarship;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this individual ship.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The birthplace of this ship.")
    private Planet shipyard;

    @Nonnull
    @JsonProperty
    @Schema(description = "The fleet which this ship is part of.")
    private Integer idFleet;

    @Nullable
    @JsonProperty
    @Schema(description = "The mission which this ship is part of.")
    private Integer idMission;

    @JsonProperty
    @Schema(required = true, description = "If the ship is part of the reserve.")
    private boolean isPooled = true;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ship class which this ship is a type of.")
    private ShipClass shipClass;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ship class which this ship is a type of.")
    private WarshipHealthState warshipHealthState;

    public WarShip() {
    }

    public WarShip(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip,
                   @Nonnull final WarshipHealthStateAccessor healthState,
                   @Nonnull final String languageCode) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");
        Preconditions.checkNotNull(healthState, "healthState must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idWarship = warShip.getId();
        this.name = warShip.getName();
        this.shipyard = new de.yuga.spacebattle.rest.dto.orbitals.Planet(warShip.getShipyard());
        setDetachment(warShip);
        this.shipClass = new de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass(warShip.getShipClass(), languageCode);
        this.warshipHealthState = new WarshipHealthState(healthState, languageCode);
    }

    @JsonIgnore
    private void setDetachment(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        final Fleet fleet = warShip.getFleet();
        if (fleet != null) {
            this.idFleet = fleet.getId();
            isPooled = false;
        }
        final Mission mission = warShip.getMission();
        if (mission != null) {
            this.idMission = mission.getId();
            isPooled = false;
        }
    }

    public WarShip(@Nonnull final WarshipHealthStateSnapshot stateSnapshot, @Nonnull final String languageCode) {

        final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip = stateSnapshot.getWarShip();
        this.idWarship = warShip.getId();
        this.name = warShip.getName();
        this.shipyard = new de.yuga.spacebattle.rest.dto.orbitals.Planet(warShip.getShipyard());
        setDetachment(warShip);
        this.shipClass = new de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass(warShip.getShipClass(), languageCode);
        this.warshipHealthState = new WarshipHealthState(stateSnapshot, languageCode);
    }

    public int getIdWarship() {
        return idWarship;
    }
}
