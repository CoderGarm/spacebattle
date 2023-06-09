package de.yuga.spacebattle.rest.dto.orbitals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EPlanetClassType;
import de.yuga.spacebattle.rest.dto.account.Player;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Schema(description = ".")
public class Planet {

    @Nonnull
    @Schema(required = true, description = "The ID of this planet.")
    private Integer idPlanet;

    @Nullable
    @Schema(description = "The owner of this planet, if already colonized.")
    private Player owner;

    @JsonProperty
    @Schema(required = true, description = "If this planet is the main planet of the owner.")
    private boolean isMain;

    @Nonnull
    @Schema(required = true, description = "The name of this planet.")
    private String name;

    @Schema(required = true, description = "The star system ID which this planet is part of.")
    private int idStarSystem;

    @Nonnull
    @Schema(required = true, description = "The orbit inside of the parent system.")
    private Orbit orbit;

    @Nullable
    @Schema(description = "The timestamp when this planet was colonized first.")
    private LocalDateTime colonizedAt;

    @Nonnull
    @Schema(required = true, description = "The planet's class.")
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
            this.owner = new Player(planet.getOwner());
            this.colonizedAt = planet.getColonizedAt();
            this.isMain = planet.isMain();
        }
        planetType = new de.yuga.spacebattle.rest.dto.enums.EPlanetClassType(planet.getPlanetType());
    }

    public int getIdPlanet() {
        return idPlanet;
    }

    @Nullable
    public Player getOwner() {
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
