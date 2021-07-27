package de.yuga.spacebattle.rest.dto.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Construction {

    /**
     * If this is null, the information about the entity must be fetched.
     */
    @Nullable
    @ApiModelProperty(required = true, value = "The ID of this construction.")
    private Integer idConstruction;

    @Nonnull
    @ApiModelProperty(required = true, value = "The building which is the base of this construction.")
    private Building building;

    @ApiModelProperty(required = true, value = "The level of this construction.")
    private int level;

    public Construction() {
    }

    public Construction(@Nonnull final de.yuga.spacebattle.backend.entities.buildings.Building building,
                        final int level) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        this.building = new Building(building);
        this.level = level;
    }

    public Construction(@Nonnull final de.yuga.spacebattle.backend.entities.constructables.buildings.Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        this.idConstruction = construction.getId();
        this.building = new Building(construction.getBuilding());
        this.level = construction.getLevel();
    }

    @Nullable
    public Integer getIdConstruction() {
        return idConstruction;
    }

    @Nonnull
    public Building getBuilding() {
        return building;
    }

    public int getLevel() {
        return level;
    }
}
