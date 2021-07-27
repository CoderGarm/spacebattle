package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class WarShip {

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this individual ship.")
    private String name;

    @Nonnull
    @ApiModelProperty(required = true, value = "The birthplace of this ship.")
    private Planet shipyard;

    @Nonnull
    @ApiModelProperty(required = true, value = "The fleet which this ship is part of.")
    private int idFleet;

    @Nonnull
    @ApiModelProperty(required = true, value = "The ship class which this ship is a type of.")
    private ShipClass shipClass;

    public WarShip() {
    }

    public WarShip(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        this.name = warShip.getName();
        this.shipyard = new de.yuga.spacebattle.rest.dto.orbitals.Planet(warShip.getShipyard());
        this.idFleet = warShip.getFleet().getId();
        this.shipClass = new de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass(warShip.getShipClass());
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
