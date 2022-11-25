package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TransportJob {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Planet from;

    @Nonnull
    private final Planet to;

    @Nonnull
    private final Map<EResourceType, Long> resources = new HashMap<>();

    @Nonnull
    private final Map<EEducationType, Long> humanResources = new HashMap<>();

    public TransportJob(@Nonnull final Tick today,
                        @Nonnull final Planet from,
                        @Nonnull final Planet to) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(to, "to must not be empty");

        this.today = today;
        this.from = from;
        this.to = to;
    }

    public void add(@Nonnull final EResourceType what,
                    final long amount) {
        Preconditions.checkNotNull(what, "what must not be empty");

        this.resources.put(what, amount);
    }

    public void add(@Nonnull final EEducationType what,
                    final long amount) {
        Preconditions.checkNotNull(what, "what must not be empty");

        this.humanResources.put(what, amount);
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Planet getFrom() {
        return from;
    }

    @Nonnull
    public Planet getTo() {
        return to;
    }

    @Nonnull
    public Map<EResourceType, Long> getResources() {
        return resources;
    }

    @Nonnull
    public Map<EEducationType, Long> getHumanResources() {
        return humanResources;
    }

    public boolean isToday(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return today.equals(tick);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final TransportJob that = (TransportJob) o;

        return new EqualsBuilder().append(today, that.today).append(from, that.from).append(to, that.to).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(today).append(from).append(to).toHashCode();
    }
}
