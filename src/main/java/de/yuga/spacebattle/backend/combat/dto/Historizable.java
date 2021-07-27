package de.yuga.spacebattle.backend.combat.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

/**
 * A unique and cloneable object definition.
 */
public class Historizable<Type extends Cloneable> implements Cloneable {

    private UUID uuid = UUID.randomUUID();

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Historizable)) return false;
        final Historizable<?> that = (Historizable<?>) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Historizable<Type> clone() {
        try {
            final Historizable clone = (Historizable) super.clone();
            clone.uuid = UUID.fromString(getUuid().toString());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
