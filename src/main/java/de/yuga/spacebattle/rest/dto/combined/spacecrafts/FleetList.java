package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FleetList extends ArrayList<Fleet> {

    public FleetList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleetList) {
        Preconditions.checkNotNull(fleetList, "fleetList shouldn't be null!");

        final List<Fleet> shipClassList = fleetList.stream().map(Fleet::new).collect(Collectors.toList());
        addAll(shipClassList);
    }
}
