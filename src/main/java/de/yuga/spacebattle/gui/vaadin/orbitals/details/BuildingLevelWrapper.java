package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link Building} and it's level.
 */
public class BuildingLevelWrapper {

    @Nonnull
    private final Building building;

    @Nonnull
    private Integer level;

    public BuildingLevelWrapper(@Nonnull final Building building, @Nonnull final Integer level) {
        Preconditions.checkNotNull(building, "module shouldn't be null!");
        Preconditions.checkNotNull(level, "amount shouldn't be null!");

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

    public String getLevelString() {
        return "Level: " + level;
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<Building, Integer> getAsEntry() {
        return new Map.Entry<Building, Integer>() {
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
}
