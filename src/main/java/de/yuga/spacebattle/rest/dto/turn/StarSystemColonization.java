package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caclulator.NavigationCalculatorService;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.orbitals.StarSystem.STAR_SYSTEM_STANDARD_METRIC;

@Schema(description = ".")
public class StarSystemColonization {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The star system to select for colonization.")
    private StarSystem starSystem;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The star system with its distance to all known systems by id.")
    private Map<Integer, Distance> distanceMap;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The star system with its travel time to all known systems by id.")
    private Map<Integer, Integer> travelTimeMap;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The costs to buy the colonization information about the system.")
    private String costsToBuyColonizationInformation;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The costs to colonize the planet by idPlanet.")
    private final Map<Integer, ResourceDeposit> costsToColonization = new HashMap<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The costs to colonize the planet by idPlanet.")
    private Map<Integer, Colonization> colonizationsByPlanet;

    public StarSystemColonization() {
    }

    public StarSystemColonization(@Nonnull final NavigationCalculatorService navigationCalculatorService,
                                  @Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem,
                                  @Nonnull final Collection<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownSystems,
                                  @Nonnull final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizations,
                                  @Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem homeSystem) {
        Preconditions.checkNotNull(navigationCalculatorService, "navigationCalculatorService must not be empty");
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");
        Preconditions.checkNotNull(knownSystems, "knownSystems shouldn't be null!");
        Preconditions.checkNotNull(colonizations, "colonizations shouldn't be null!");
        Preconditions.checkNotNull(homeSystem, "homeSystem must not be empty");

        this.starSystem = new StarSystem(starSystem);
        this.distanceMap = knownSystems
                .stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId,
                        sys -> DistanceCalculator.getOrbitalDistance(starSystem.getOrbit(), sys.getOrbit()).convertToMetric(STAR_SYSTEM_STANDARD_METRIC)));

        this.travelTimeMap = knownSystems
                .stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId,
                        sys -> navigationCalculatorService.getTimeToTravel(homeSystem, sys)));

        costsToBuyColonizationInformation = setColoInformationCosts(starSystem);
        setColonizationCosts(starSystem);
        colonizationsByPlanet = colonizations.stream().collect(Collectors.toMap(c -> c.getTarget().getId(), Colonization::new));
    }

    @JsonIgnore
    private void setColonizationCosts(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        starSystem.getPlanets().forEach(planet -> {
            final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs = ColonizationCostCalculator.getColonizationCosts(planet);
            costsToColonization.put(planet.getId(), new ResourceDeposit(costs));
        });
    }

    @JsonIgnore
    private String setColoInformationCosts(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final ResourceAmount costsDTO = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final EResourceType resourceType = costsDTO.getRealType();
        final Long amountWithDiff = costsDTO.getAmount();
        return amountWithDiff + " " + resourceType;
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
    public Map<Integer, ResourceDeposit> getCostsToColonization() {
        return costsToColonization;
    }

    @Nonnull
    public Map<Integer, Colonization> getColonizationsByPlanet() {
        return colonizationsByPlanet;
    }
}
