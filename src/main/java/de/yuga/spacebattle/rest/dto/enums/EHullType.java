package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

/**
 * This indicated what a type of ship is this hull for.
 */
@ApiModel(parent = HasIcon.class)
public class EHullType extends HasIcon {

    @Nonnull
    @ApiModelProperty(required = true, value = "The hulls type.")
    private final String type;

    @JsonProperty
    @ApiModelProperty(required = true, value = "If the hull is a pod layer.")
    private final boolean podLayer;

    @JsonProperty
    @ApiModelProperty(required = true, value = "If the hull is an auxiliary ship.")
    private final boolean auxiliaryShip;

    @Nonnull
    @ApiModelProperty(required = true, value = "The hulls description.")
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
