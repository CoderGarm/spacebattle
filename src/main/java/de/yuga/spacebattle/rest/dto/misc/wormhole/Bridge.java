package de.yuga.spacebattle.rest.dto.misc.wormhole;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Bridge {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The one position of the bridge.")
    private Coords a;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The other position of the bridge.")
    private Coords b;

    public Bridge(@Nonnull final Coords a, @Nonnull final Coords b) {
        this.a = a;
        this.b = b;
    }
}
