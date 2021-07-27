package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

@ApiModel(parent = HasIcon.class)
public class EStarClassType extends HasIcon {

    /**
     * Spectral class	Limit in light minutes
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The spectral class.")
    private final String spectralClass;

    @ApiModelProperty(required = true, value = "The hyper limit in light minutes..")
    private final double lightMinutesToHyperLimit;

    public EStarClassType() {
        super();
        this.spectralClass = "";
        this.lightMinutesToHyperLimit = Integer.MIN_VALUE;
    }

    public EStarClassType(@Nonnull final de.yuga.spacebattle.backend.enums.EStarClassType starClassType) {
        super(starClassType);

        this.spectralClass = starClassType.getSpectralClass();
        this.lightMinutesToHyperLimit = starClassType.getLightMinutesToHyperLimit();
    }

    @Nonnull
    public String getSpectralClass() {
        return spectralClass;
    }

    public double getLightMinutesToHyperLimit() {
        return lightMinutesToHyperLimit;
    }
}
