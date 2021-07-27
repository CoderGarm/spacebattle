package de.yuga.spacebattle.rest.dto.researches;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResearchLevelList extends ArrayList<ResearchLevel> {

    public ResearchLevelList(@Nonnull final Map<de.yuga.spacebattle.backend.entities.researches.Research, Integer> researches) {
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        final List<ResearchLevel> shipClassList = researches.entrySet()
                .stream().map(entry -> new ResearchLevel(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        addAll(shipClassList);
    }
}
