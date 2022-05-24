package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EStarClassType extends HasIcon {

    /**
     * Spectral class	Limit in light minutes
     */
    @Nonnull
    @Schema(required = true, description = "The spectral class.")
    private final String spectralClass;

    @Schema(required = true, description = "The hyper limit in light minutes..")
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
