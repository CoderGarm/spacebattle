package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class CapacityValue {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value's type.")
    private ECapacityAreaType capacityArea;

    @JsonProperty
    @Schema(required = true, description = "The used capacity in that area.")
    private int usedCapacity;

    @JsonProperty
    @Schema(required = true, description = "The overall capacity in that area.")
    private int capacity;

    public CapacityValue() {
    }

    public CapacityValue(@Nonnull final de.yuga.spacebattle.backend.enums.ECapacityAreaType capacityArea,
                         final int usedCapacity,
                         final int capacity) {
        Preconditions.checkNotNull(capacityArea, "moduleType shouldn't be null!");

        this.capacityArea = capacityArea;
        this.usedCapacity = usedCapacity;
        this.capacity = capacity;
    }

    @JsonIgnore
    public void setCapacityArea(@Nonnull final ECapacityAreaType capacityArea) {
        Preconditions.checkNotNull(capacityArea, "capacityArea must not be empty");

        this.capacityArea = capacityArea;
    }

    @JsonIgnore
    public void setUsedCapacity(final int usedCapacity) {
        this.usedCapacity = usedCapacity;
    }

    @JsonIgnore
    public void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "capacityArea: " + capacityArea + ", value: " + usedCapacity;
    }
}
