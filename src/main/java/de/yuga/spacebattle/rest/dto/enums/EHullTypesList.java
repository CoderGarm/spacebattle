package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EHullTypesList extends ArrayList<EHullType> {

    public EHullTypesList(@Nonnull final List<de.yuga.spacebattle.backend.enums.EHullType> types) {
        Preconditions.checkNotNull(types, "types shouldn't be null!");

        addAll(types.stream().map(EHullType::new).collect(Collectors.toList()));
    }
}
