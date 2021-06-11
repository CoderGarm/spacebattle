package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EPlanetType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link Building} and it's level.
 */
public class PlanetIconDTO implements ImageMapper {

    @Nonnull
    private final Planet planet;

    public PlanetIconDTO(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        this.planet = planet;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    @Nonnull
    public String getPlanetName() {
        return getPlanet().getName();
    }

    @Nonnull
    public String getPlanetClass() {
        return "Class " + getPlanet().getPlanetType().getPlanetClass() + " planet";
    }

    @Override
    public String getAlternativeText() {
        return getPlanetName();
    }

    @Override
    public String getTitleText() {
        return getPlanetName();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EPlanetType resourceType = getPlanet().getPlanetType();
        final String iconName = resourceType.getIconName();
        return EIconPath.getPath(resourceType, iconName, resolution.getResolution());
    }
}
