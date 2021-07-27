package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EModuleTypeList extends ArrayList<EModuleType> {

    public EModuleTypeList(@Nonnull final List<de.yuga.spacebattle.backend.enums.EModuleType> c) {
        Preconditions.checkNotNull(c, "c shouldn't be null!");

        addAll(c.stream().map(EModuleType::new).collect(Collectors.toList()));
    }
}
