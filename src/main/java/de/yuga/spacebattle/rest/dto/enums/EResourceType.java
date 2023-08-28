package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.ECollectableType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EResourceType extends HasIcon {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The collectable type of this resource.")
    private final ECollectableType collectableType;

    public EResourceType() {
        super();
        this.collectableType = ECollectableType.COLLECTABLE;
    }

    public EResourceType(@Nonnull final de.yuga.spacebattle.backend.enums.EResourceType resourceType) {
        super(resourceType);

        this.collectableType = resourceType.getCollectableType();
    }

    @Nonnull
    @JsonIgnore
    public ECollectableType getCollectableType() {
        return collectableType;
    }
}
