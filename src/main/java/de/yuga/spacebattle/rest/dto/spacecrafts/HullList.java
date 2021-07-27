package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HullList extends ArrayList<Hull> {

    public HullList(@Nonnull final List<de.yuga.spacebattle.backend.entities.spacecrafts.Hull> hulls) {
        Preconditions.checkNotNull(hulls, "hulls shouldn't be null!");

        addAll(hulls.stream().map(Hull::new).collect(Collectors.toList()));
    }
}
