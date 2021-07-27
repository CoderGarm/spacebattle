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
public class StarSystemList extends ArrayList<StarSystem> {

    public StarSystemList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> starSystems) {
        Preconditions.checkNotNull(starSystems, "starSystems shouldn't be null!");

        final List<StarSystem> transformedUsers = starSystems.stream().map(StarSystem::new).collect(Collectors.toList());
        addAll(transformedUsers);
    }
}
