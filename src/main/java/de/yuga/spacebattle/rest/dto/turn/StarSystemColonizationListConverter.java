package de.yuga.spacebattle.rest.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.services.caclulator.NavigationCalculatorService;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = ".")
public class StarSystemColonizationListConverter {


    public static List<StarSystemColonization> create(@Nonnull final NavigationCalculatorService navigationCalculatorService,
                                                      final int idUser,
                                                      @Nonnull final Collection<StarSystem> allSystems,
                                                      @Nonnull final Collection<StarSystem> knownStarSystems,
                                                      @Nonnull final Collection<Colonization> colonizationsForUser) {
        Preconditions.checkNotNull(navigationCalculatorService, "navigationCalculatorService must not be empty");
        Preconditions.checkNotNull(allSystems, "allSystems shouldn't be null!");
        Preconditions.checkNotNull(knownStarSystems, "knownStarSystems shouldn't be null!");
        Preconditions.checkNotNull(colonizationsForUser, "colonizationsForUser shouldn't be null!");

        final Map<StarSystem, List<Colonization>> starSystemColonizationMap = colonizationsForUser
                .stream()
                .collect(Collectors.groupingBy(c -> c.getTarget().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final StarSystem homeSystem = knownStarSystems.stream()
                .filter(sys -> sys.getPlanets().stream().anyMatch(planet -> planet.isMain() && Objects.requireNonNull(planet.getOwner()).getId() == idUser))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Oh my dear, you should live somewhere!"));

        return allSystems
                .stream()
                .map(sys -> {
                    final List<Colonization> colonizations = starSystemColonizationMap.computeIfAbsent(sys, starSystem -> new ArrayList<>());
                    return new StarSystemColonization(navigationCalculatorService, sys, knownStarSystems, colonizations, homeSystem);
                })
                .collect(Collectors.toList());
    }
}
