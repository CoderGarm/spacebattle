package de.yuga.spacebattle.rest.dto.orbitals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EPlanetClassType;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Schema(description = ".")
public class Planet {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ID of this planet.")
    private Integer idPlanet;

    @Nullable
    @JsonProperty
    @Schema(description = "The owner of this planet, if already colonized.")
    private Player owner;

    @JsonProperty
    @Schema(required = true, description = "If this planet is the main planet of the owner.")
    private boolean isMain;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this planet.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The star system ID which this planet is part of.")
    private AbstractId starSystem;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The orbit inside of the parent system.")
    private Orbit orbit;

    @Nullable
    @JsonProperty
    @Schema(description = "The timestamp when this planet was colonized first.")
    private LocalDateTime colonizedAt;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet's class.")
    private de.yuga.spacebattle.rest.dto.enums.EPlanetClassType planetType = new de.yuga.spacebattle.rest.dto.enums.EPlanetClassType(EPlanetClassType.PLANET);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet's usage capabilities.")
    private Set<EResourceType> capabilities = new HashSet<>();


    public Planet() {
    }

    public Planet(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        this.idPlanet = planet.getId();
        this.name = planet.getName();
        this.starSystem = new AbstractId(planet.getSystem(), planet.getSystem().getName());
        this.orbit = new Orbit(planet.getOrbit());

        if (planet.getOwner() != null) {
            this.owner = new Player(planet.getOwner());
            this.colonizedAt = planet.getColonizedAt();
            this.isMain = planet.isMain();
        }
        planetType = new de.yuga.spacebattle.rest.dto.enums.EPlanetClassType(planet.getPlanetType());
        planet.getConstructions().forEach(c -> {
            final de.yuga.spacebattle.backend.enums.EResourceType productionTarget = c.getBuilding().getProductionTarget();
            switch (productionTarget) {
                case CONSTRUCTION:
                case ORBITAL_CONSTRUCTION:
                case RESEARCH:
                    this.capabilities.add(new EResourceType(productionTarget));
                    break;
                default:
                    break;
            }
        });
    }

    public int getIdPlanet() {
        return idPlanet;
    }
}
