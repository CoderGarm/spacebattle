package de.yuga.spacebattle.rest.dto.misc;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CoordsBlob extends ArrayList<Coords> {

    public CoordsBlob(@Nonnull final List<Coords> coords) {
        Preconditions.checkNotNull(coords, "coords must not be empty");

        super.addAll(coords);
    }
}
