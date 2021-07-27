package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShipClassList extends ArrayList<ShipClass> {

    public ShipClassList(@Nonnull final List<de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses shouldn't be null!");

        addAll(shipClasses.stream().map(ShipClass::new).collect(Collectors.toList()));
    }
}
