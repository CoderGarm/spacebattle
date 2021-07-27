package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SidewallList extends ArrayList<Sidewall> {

    public SidewallList(@Nonnull final List<de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall> modules) {
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        addAll(modules.stream().map(Sidewall::new).collect(Collectors.toList()));
    }
}
