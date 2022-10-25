package de.yuga.spacebattle.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = "A container when only the database id if needed.")
public class AbstractId {

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private final int id;

    public AbstractId(@Nonnull final AbstractEntityKey entityKey) {
        Preconditions.checkNotNull(entityKey, "entityKey must not be empty");

        this.id = entityKey.getId();
    }

    public AbstractId(final int id) {
        this.id = id;
    }
}
