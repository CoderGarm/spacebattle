package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElectronicWarfareList extends ArrayList<ElectronicWarfare> {

    public ElectronicWarfareList(@Nonnull final List<de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare> modules) {
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        addAll(modules.stream().map(ElectronicWarfare::new).collect(Collectors.toList()));
    }
}
