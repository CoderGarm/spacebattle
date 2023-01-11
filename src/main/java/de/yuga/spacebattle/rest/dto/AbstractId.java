package de.yuga.spacebattle.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

@Schema(description = "A container when only the database id if needed.")
public class AbstractId {

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private final int id;

    @JsonProperty
    @Schema(description = "The name.")
    private String name = null;

    public AbstractId(@Nonnull final AbstractEntityKey entityKey) {
        Preconditions.checkNotNull(entityKey, "entityKey must not be empty");

        this.id = entityKey.getId();
    }

    public AbstractId(@Nonnull final AbstractEntityKey entityKey, @Nonnull final String name) {
        Preconditions.checkNotNull(entityKey, "entityKey must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");

        this.id = entityKey.getId();
        this.name = name;
    }

    public AbstractId(final int id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final AbstractId that = (AbstractId) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
