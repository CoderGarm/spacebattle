package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.annotations.ApiModel;

import javax.annotation.Nonnull;

@ApiModel(parent = HasIcon.class)
public class EPlanetClassType extends HasIcon {

    @Nonnull
    private final String name;

    @Nonnull
    private final String planetClass;

    public EPlanetClassType() {
        super();
        this.name = "";
        this.planetClass = "";
    }

    public EPlanetClassType(@Nonnull final de.yuga.spacebattle.backend.enums.EPlanetClassType planetClassType) {
        super(planetClassType);

        this.name = planetClassType.getName();
        this.planetClass = planetClassType.getPlanetClass();
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getPlanetClass() {
        return planetClass;
    }
}
