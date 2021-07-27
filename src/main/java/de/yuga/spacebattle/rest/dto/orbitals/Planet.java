package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EPlanetClassType;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

public class Planet {

    @Nonnull
    @ApiModelProperty(required = true, value = "The ID of this planet.")
    private Integer idPlanet;

    @Nullable
    @ApiModelProperty("The owner of this planet, if already colonized.")
    private UserJson owner;

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this planet.")
    private String name;

    @ApiModelProperty(required = true, value = "The star system ID which this planet is part of.")
    private int idStarSystem;

    @Nonnull
    @ApiModelProperty(required = true, value = "The orbit inside of the parent system.")
    private Orbit orbit;

    @Nullable
    @ApiModelProperty("The timestamp when this planet was colonized first.")
    private LocalDateTime colonizedAt;

    @Nonnull
    @ApiModelProperty(required = true, value = "The planet's class.")
    private de.yuga.spacebattle.rest.dto.enums.EPlanetClassType planetType = new de.yuga.spacebattle.rest.dto.enums.EPlanetClassType(EPlanetClassType.PLANET);

    public Planet() {
    }

    public Planet(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        this.idPlanet = planet.getId();
        this.name = planet.getName();
        this.idStarSystem = planet.getSystem().getId();
        this.orbit = new Orbit(planet.getOrbit());

        if (planet.getOwner() != null) {
            this.owner = new UserJson(planet.getOwner());
            this.colonizedAt = planet.getColonizedAt();
        }
        planetType = new de.yuga.spacebattle.rest.dto.enums.EPlanetClassType(planet.getPlanetType());
    }

    public int getIdPlanet() {
        return idPlanet;
    }

    @Nullable
    public UserJson getOwner() {
        return owner;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getIdStarSystem() {
        return idStarSystem;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    @Nullable
    public LocalDateTime getColonizedAt() {
        return colonizedAt;
    }

    @Nonnull
    public de.yuga.spacebattle.rest.dto.enums.EPlanetClassType getPlanetType() {
        return planetType;
    }
}
