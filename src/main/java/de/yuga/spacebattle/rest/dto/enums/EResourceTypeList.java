package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EResourceTypeList extends ArrayList<EResourceType> {

    public EResourceTypeList(@Nonnull final List<de.yuga.spacebattle.backend.enums.EResourceType> eResourceTypes) {
        Preconditions.checkNotNull(eResourceTypes, "eResourceTypes shouldn't be null!");

        addAll(eResourceTypes.stream().map(EResourceType::new).collect(Collectors.toList()));
    }
}
