package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * This indicated what a type of ship is this hull for.
 */
@Schema
public class EHullType extends HasIcon {

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

    public EHullType() {
        super();
        description = "";
        podLayer = false;
        auxiliaryShip = false;
        type = "";
    }

    public EHullType(@Nonnull final de.yuga.spacebattle.backend.enums.EHullType hullType) {
        super(hullType);

        this.type = hullType.getType();
        this.podLayer = hullType.isPodLayer();
        this.auxiliaryShip = hullType.isAuxiliaryShip();
        this.description = hullType.getDescription();
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
