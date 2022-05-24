package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class StarSystem {

    @Nonnull
    @Schema(required = true, description = "The ID of this star system.")
    private Integer idStarSystem;

    @Nonnull
    @Schema(required = true, description = "The name of this star system.")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The orbit of this star system.")
    private Orbit orbit;

    @Nonnull
    @Schema(required = true, description = "The stellar class of the star of this system.")
    private de.yuga.spacebattle.rest.dto.enums.EStarClassType starClassType;

    @Nonnull
    @Schema(required = true, description = "The bunch of planets as part of the system.")
    private final List<Planet> planets = new ArrayList<>();

    public StarSystem() {
    }

    public StarSystem(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        this.idStarSystem = starSystem.getId();
        this.name = starSystem.getName();
        this.orbit = new Orbit(starSystem.getOrbit());
        this.starClassType = new de.yuga.spacebattle.rest.dto.enums.EStarClassType(starSystem.getStarClassType());
        this.planets.addAll(starSystem.getPlanets().stream().map(Planet::new).collect(Collectors.toList()));
    }

    @Nonnull
    public Integer getIdStarSystem() {
        return idStarSystem;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    @Nonnull
    public de.yuga.spacebattle.rest.dto.enums.EStarClassType getStarClassType() {
        return starClassType;
    }

    @Nonnull
    public List<Planet> getPlanets() {
        return planets;
    }
}
