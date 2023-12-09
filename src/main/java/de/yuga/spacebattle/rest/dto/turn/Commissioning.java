package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.rest.dto.turn.resources.WarshipsByFleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Commissioning {

    @Nonnull
    @JsonProperty
    @Schema(description = "The planet's name.")
    private String planet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The systems's name.")
    private String system;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The constructions which are newly active.")
    private List<de.yuga.spacebattle.rest.dto.constructables.buildings.Construction> constructions = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The orbital constructions which are newly active.")
    private List<OrbitalModule> orbitalModules = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The activated warships to their fleet.")
    private final List<WarshipsByFleet> warships = new ArrayList<>();

    public Commissioning(@Nonnull final de.yuga.spacebattle.backend.dto.turn.Commissioning commissioning,
                         @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(commissioning, "operational must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        planet = commissioning.getPlanet().getName();
        system = commissioning.getPlanet().getSystem().getName();

        final Set<Construction> constructions = commissioning.getConstructions();
        if (!constructions.isEmpty()) {
            this.constructions = constructions.stream()
                    .map(c -> new de.yuga.spacebattle.rest.dto.constructables.buildings.Construction(c, preferredLanguage))
                    .collect(Collectors.toList());
        }

        final List<OrbitalStructure> orbitalStructures = commissioning.getOrbitalStructures();
        if (!orbitalStructures.isEmpty()) {
            for (final OrbitalStructure orbitalStructure : orbitalStructures) {
                final de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule module = orbitalStructure.getModule();
                final int amount = orbitalStructure.getAmount();
                for (int i = 0; i < amount; i++) {
                    this.orbitalModules.add(new OrbitalModule(module, preferredLanguage));
                }
            }
        }

        final List<WarShip> warships = commissioning.getWarships();
        warships.stream().filter(w -> Objects.nonNull(w.getFleet()))
                .collect(Collectors.groupingBy(WarShip::getFleet,
                        Collectors.mapping(Function.identity(), Collectors.toList())))
                .forEach((fleet, warShips) -> this.warships.add(new WarshipsByFleet(fleet.getName(),
                        warShips.stream().map(w -> new de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip(w, w.getWarshipHealthState(), preferredLanguage))
                                .collect(Collectors.toList()))));

        final List<WarShip> pooledShips = warships.stream().filter(w -> Objects.isNull(w.getFleet()))
                .collect(Collectors.toList());
        if (!pooledShips.isEmpty()) {
            this.warships.add(new WarshipsByFleet("Reserve", pooledShips.stream()
                    .map(w -> new de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip(w, w.getWarshipHealthState(), preferredLanguage))
                    .collect(Collectors.toList())));
        }
    }
}
