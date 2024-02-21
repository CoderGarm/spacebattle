package de.yuga.spacebattle.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = "A container when only the database id if needed.")
public class PlanetAbstractId {

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private int idPlanet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name.")
    private String name;

    @JsonProperty
    @Schema(required = true, description = ".")
    private boolean isMain;

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private int idStarSystem;

    public PlanetAbstractId() {
    }

    public PlanetAbstractId(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.idPlanet = planet.getId();
        this.name = planet.getName();
        this.isMain = planet.isMain();
        this.idStarSystem = planet.getSystem().getId();
    }

    public PlanetAbstractId(final int idPlanet, @Nonnull final String name, final boolean isMain, final int idStarSystem) {
        this.idPlanet = idPlanet;
        this.name = Preconditions.checkNotNull(name, "name must not be empty");
        this.isMain = isMain;
        this.idStarSystem = idStarSystem;
    }
}
