package de.yuga.spacebattle.rest.dto.researches;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ResearchList extends ArrayList<Research> {

    public ResearchList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.researches.Research> fleetList) {
        Preconditions.checkNotNull(fleetList, "fleetList shouldn't be null!");

        final List<Research> shipClassList = fleetList.stream().map(Research::new).collect(Collectors.toList());
        addAll(shipClassList);
    }
}
