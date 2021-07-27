package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.ECollectableType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

@ApiModel(parent = HasIcon.class)
public class EResourceType extends HasIcon {

    @Nonnull
    @ApiModelProperty(required = true, value = "The singular name of this resource.")
    private final String singularName;

    @Nonnull
    @ApiModelProperty(required = true, value = "The plural name of this resource.")
    private final String pluralName;

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The collectable type of this resource.")
    private final ECollectableType collectableType;

    public EResourceType() {
        super();
        this.singularName = "";
        this.pluralName = "";
        this.collectableType = ECollectableType.COLLECTABLE;
    }

    public EResourceType(@Nonnull final de.yuga.spacebattle.backend.enums.EResourceType resourceType) {
        super(resourceType);

        this.singularName = resourceType.getSingularName();
        this.pluralName = resourceType.getPluralName();
        this.collectableType = resourceType.getCollectableType();
    }

    @Nonnull
    public String getSingularName() {
        return singularName;
    }

    @Nonnull
    public String getPluralName() {
        return pluralName;
    }

    @Nonnull
    @JsonIgnore
    public ECollectableType getCollectableType() {
        return collectableType;
    }
}
