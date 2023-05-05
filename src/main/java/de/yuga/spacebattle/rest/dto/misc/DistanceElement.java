package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DistanceElement implements Comparable<DistanceElement> {

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final String name;

    @Nullable
    @JsonProperty
    @Schema
    private Position position;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final Map<DistanceElement, Integer> connections = new HashMap<>();

    public DistanceElement(@Nonnull final String name) {
        this.name = Preconditions.checkNotNull(name, "name must not be empty");
    }

    @Nonnull
    @JsonIgnore
    public String getName() {
        return name;
    }

    @Nullable
    @JsonIgnore
    public Position getPosition() {
        return position;
    }

    @JsonIgnore
    public void setPosition(@Nonnull final Position position) {
        this.position = Preconditions.checkNotNull(position, "position must not be empty");
    }

    @Nonnull
    @JsonIgnore
    public Map<DistanceElement, Integer> getConnections() {
        return connections;
    }

    @Nonnull
    @JsonIgnore
    public Map<DistanceElement, Integer> getConnectionsWithCoordinates() {
        return connections.entrySet().stream().filter(e -> e.getKey().getPosition() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final DistanceElement that = (DistanceElement) o;

        return new EqualsBuilder().append(name, that.name).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).toHashCode();
    }

    @Override
    @JsonIgnore
    public String toString() {
        return name;
    }

    @JsonIgnore
    public void add(@Nonnull final DistanceElement connectedTo, final int distance) {
        Preconditions.checkNotNull(connectedTo, "connectedTo must not be empty");

        connections.put(connectedTo, distance);
    }

    @Override
    @JsonIgnore
    public int compareTo(@Nonnull final DistanceElement o) {
        return name.compareTo(o.getName());
    }
}
