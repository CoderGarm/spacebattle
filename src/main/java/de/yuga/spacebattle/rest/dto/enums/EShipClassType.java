package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * This indicated what a type of ship is this hull for.
 */
@Schema(description = ".")
public class EShipClassType extends HasIcon {

    @Nonnull
    @Schema(required = true, description = "The hulls type.")
    private final String type;

    @JsonProperty
    @Schema(required = true, description = "If the hull is a pod layer.")
    private final boolean podLayer;

    @JsonProperty
    @Schema(required = true, description = "If the hull is an auxiliary ship.")
    private final boolean auxiliaryShip;

    @Nonnull
    @Schema(required = true, description = "The hulls description.")
    private final String description;

    public EShipClassType() {
        super();
        description = "";
        podLayer = false;
        auxiliaryShip = false;
        type = "";
    }

    public EShipClassType(@Nonnull final de.yuga.spacebattle.backend.enums.EShipClassType shipClassType) {
        super(shipClassType);

        this.type = shipClassType.getType();
        this.podLayer = shipClassType.isPodLayer();
        this.auxiliaryShip = shipClassType.isAuxiliaryShip();
        this.description = shipClassType.getDescription();
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @JsonIgnore
    public boolean isPodLayer() {
        return podLayer;
    }

    @JsonIgnore
    public boolean isAuxiliaryShip() {
        return auxiliaryShip;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }
}
