package de.yuga.spacebattle.rest.dto.constructables.buildings;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructionList extends ArrayList<Construction> {

    public ConstructionList(@Nonnull final List<de.yuga.spacebattle.backend.entities.constructables.buildings.Construction> planetList) {
        Preconditions.checkNotNull(planetList, "planetList shouldn't be null!");

        final List<Construction> transformedUsers = planetList.stream().map(Construction::new).collect(Collectors.toList());
        addAll(transformedUsers);
    }

    public ConstructionList(Collection<Construction> constructions) {
        Preconditions.checkNotNull(constructions, "constructions shouldn't be null!");

        addAll(constructions);
    }
}
