package de.yuga.spacebattle.rest.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = ".")
public class StarSystemColonizationListConverter {


    public static List<StarSystemColonization> create(@Nonnull final Collection<StarSystem> allSystems,
                                                      @Nonnull final Collection<StarSystem> knownStarSystems,
                                                      @Nonnull final Collection<Colonization> colonizationsForUser) {
        Preconditions.checkNotNull(allSystems, "allSystems shouldn't be null!");
        Preconditions.checkNotNull(knownStarSystems, "knownStarSystems shouldn't be null!");
        Preconditions.checkNotNull(colonizationsForUser, "colonizationsForUser shouldn't be null!");

        final Map<StarSystem, List<Colonization>> starSystemColonizationMap = colonizationsForUser
                .stream()
                .collect(Collectors.groupingBy(c -> c.getTarget().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        return allSystems
                .stream()
                .map(sys -> {
                    final List<Colonization> colonizations = starSystemColonizationMap.computeIfAbsent(sys, starSystem -> new ArrayList<>());
                    return new StarSystemColonization(sys, knownStarSystems, colonizations);
                })
                .collect(Collectors.toList());
    }
}
