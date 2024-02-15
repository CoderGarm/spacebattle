package de.yuga.spacebattle.backend.combat.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DamageDealer {

    @Nonnull
    private UUID uuid = UUID.randomUUID();

    @Nonnull
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DamageDealer)) return false;
        final DamageDealer that = (DamageDealer) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }

    @Override
    public DamageDealer clone() {
        try {
            final DamageDealer clone = (DamageDealer) super.clone();
            clone.uuid = UUID.fromString(getUuid().toString());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
