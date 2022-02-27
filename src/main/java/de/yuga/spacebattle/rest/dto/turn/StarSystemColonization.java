package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.orbitals.StarSystem.STAR_SYSTEM_STANDARD_METRIC;

public class StarSystemColonization {

    @Nonnull
    @ApiModelProperty(required = true, value = "The star system to select for colonization.")
    private final StarSystem starSystem;

    @Nonnull
    @ApiModelProperty(required = true, value = "The star system with its distance to all known systems by id.")
    private final Map<Integer, Distance> distanceMap;

    @Nonnull
    @ApiModelProperty(required = true, value = "The costs to buy the colonization information about the system.")
    private final String costsToBuyColonizationInformation;

    @Nonnull
    @ApiModelProperty(required = true, value = "The costs to colonize the planet by idPlanet.")
    private final Map<Integer, String> costsToColonization = new HashMap<>();

    @Nonnull
    @ApiModelProperty(required = true, value = "The costs to colonize the planet by idPlanet.")
    private final Map<Integer, Colonization> colonizationsByPlanet;

    public StarSystemColonization(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem,
                                  @Nonnull final Collection<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownSystems,
                                  @Nonnull final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizations) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");
        Preconditions.checkNotNull(knownSystems, "knownSystems shouldn't be null!");
        Preconditions.checkNotNull(colonizations, "colonizations shouldn't be null!");

        this.starSystem = new StarSystem(starSystem);
        this.distanceMap = knownSystems
                .stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId,
                        sys -> DistanceCalculator.getOrbitalDistance(starSystem.getOrbit(), sys.getOrbit()).convertToMetric(STAR_SYSTEM_STANDARD_METRIC)));

        costsToBuyColonizationInformation = setColoInformationCosts(starSystem);
        setColonizationCosts(starSystem);
        colonizationsByPlanet = colonizations.stream().collect(Collectors.toMap(c -> c.getTarget().getId(), Colonization::new));
    }

    @JsonIgnore
    private void setColonizationCosts(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        starSystem.getPlanets().forEach(planet -> {
            final ResourceAmount costsDTO = ColonizationCostCalculator.calculateColonizationCost(planet);
            final EResourceType resourceType = costsDTO.getRealResourceType();
            final Long amountWithDiff = costsDTO.getAmount();
            costsToColonization.put(planet.getId(), amountWithDiff + " " + resourceType.getPluralName());
        });
    }

    @JsonIgnore
    private String setColoInformationCosts(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final ResourceAmount costsDTO = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final EResourceType resourceType = costsDTO.getRealResourceType();
        final Long amountWithDiff = costsDTO.getAmount();
        return amountWithDiff + " " + resourceType.getPluralName();
    }

    @Nonnull
    public StarSystem getStarSystem() {
        return starSystem;
    }

    @Nonnull
    public Map<Integer, Distance> getDistanceMap() {
        return distanceMap;
    }

    @Nonnull
    public String getCostsToBuyColonizationInformation() {
        return costsToBuyColonizationInformation;
    }

    @Nonnull
    public Map<Integer, String> getCostsToColonization() {
        return costsToColonization;
    }

    @Nonnull
    public Map<Integer, Colonization> getColonizationsByPlanet() {
        return colonizationsByPlanet;
    }
}
