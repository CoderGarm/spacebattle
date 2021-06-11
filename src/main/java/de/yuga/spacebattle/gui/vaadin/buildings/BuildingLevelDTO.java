package de.yuga.spacebattle.gui.vaadin.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EBuildingType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link Building}, it's level and the planet where it could be located.
 */
public class BuildingLevelDTO implements ImageMapper {

    @Nonnull
    private final Planet planet;

    @Nonnull
    private final Building building;

    @Nonnull
    private Integer level;

    public BuildingLevelDTO(@Nonnull final Planet planet, @Nonnull final Building building, @Nonnull final Integer level) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(building, "building shouldn't be null!");
        Preconditions.checkNotNull(level, "amount shouldn't be null!");

        this.planet = planet;
        this.building = building;
        this.level = level;
    }

    @Nonnull
    public Building getBuilding() {
        return building;
    }

    @Nonnull
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@Nonnull final Integer level) {
        Preconditions.checkNotNull(level, "level shouldn't be null!");

        this.level = level;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    public String getLevelString() {
        return "" + level;
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<Building, Integer> getAsEntry() {
        return new Map.Entry<>() {
            @Override
            public Building getKey() {
                return building;
            }

            @Override
            public Integer getValue() {
                return level;
            }

            @Override
            public Integer setValue(Integer value) {
                level = value;
                return level;
            }
        };
    }

    @Override
    public String getAlternativeText() {
        return getBuilding().getName();
    }

    @Override
    public String getTitleText() {
        return getBuilding().getName();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EBuildingType resourceType = getBuilding().getBuildingType();
        final String iconName = resourceType.getIconName();
        return EIconPath.getPath(resourceType, iconName, resolution.getResolution());
    }
}
