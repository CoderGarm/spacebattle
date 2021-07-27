package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.SpacebattleApplication;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ridicules representation of a list for a user json - just for the fucked up swagger code gen.
 * Think about registering new classes in {@link SpacebattleApplication#api()}.
 */
public class PlanetList extends ArrayList<Planet> {

    public PlanetList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.orbitals.Planet> planetList) {
        Preconditions.checkNotNull(planetList, "planetList shouldn't be null!");

        final List<Planet> transformedUsers = planetList.stream().map(Planet::new).collect(Collectors.toList());
        addAll(transformedUsers);
    }
}
