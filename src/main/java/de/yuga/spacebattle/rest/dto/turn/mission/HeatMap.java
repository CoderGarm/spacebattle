package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Schema(description = ".")
public class HeatMap {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The universe heat map. idStarSystem to every idPlanet with its heat.")
    private Map<Integer, Map<Integer, Integer>> heatMap = new HashMap<>();

    public HeatMap() {
    }

    public HeatMap(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.mission.HeatMap> heatMaps) {
        Preconditions.checkNotNull(heatMaps, "heatMaps must not be empty");

        heatMaps.forEach(map -> {
            final Planet planet = map.getPlanet();
            final StarSystem system = planet.getSystem();
            final int heat = map.getHeat();
            final Map<Integer, Integer> data = this.heatMap.getOrDefault(system.getId(), new HashMap<>());
            data.put(planet.getId(), heat);
            this.heatMap.put(system.getId(), data);
        });
    }

}
