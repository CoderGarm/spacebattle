package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class CapacityValue implements Comparable<CapacityValue> {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value's type.")
    private ECapacityAreaType capacityArea;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The used capacity in that area.")
    private Mass tonnage;

    public CapacityValue() {
    }

    public CapacityValue(@Nonnull final de.yuga.spacebattle.backend.enums.ECapacityAreaType capacityArea,
                         @Nonnull final Mass tonnage) {
        Preconditions.checkNotNull(capacityArea, "moduleType shouldn't be null!");
        Preconditions.checkNotNull(tonnage, "tonnage must not be empty");

        this.capacityArea = capacityArea;
        this.tonnage = tonnage;
    }

    @JsonIgnore
    public void setCapacityArea(@Nonnull final ECapacityAreaType capacityArea) {
        Preconditions.checkNotNull(capacityArea, "capacityArea must not be empty");

        this.capacityArea = capacityArea;
    }

    @JsonIgnore
    public void setTonnage(@Nonnull final Mass tonnage) {
        this.tonnage = Preconditions.checkNotNull(tonnage, "tonnage must not be empty");
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "capacityArea: " + capacityArea + ", value: " + tonnage;
    }

    @Override
    @JsonIgnore
    public int compareTo(@Nonnull final CapacityValue o) {
        return ECapacityAreaType.compare(this.capacityArea, o.capacityArea);
    }
}
