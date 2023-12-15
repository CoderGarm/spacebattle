package de.yuga.spacebattle.rest.dto.misc.wormhole;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WormholeNexus {

    @Nonnull
    private final String nexusName;

    @Nonnull
    private final Set<String> terminiNames;

    public WormholeNexus(@Nonnull final String nexusName, @Nonnull final String... terminiNames) {
        Preconditions.checkNotNull(nexusName, "nexusName must not be empty");
        Preconditions.checkNotNull(terminiNames, "terminiNames must not be empty");

        this.nexusName = nexusName;
        this.terminiNames = Arrays.stream(terminiNames).collect(Collectors.toSet());
    }

    @Nonnull
    public String getNexusName() {
        return nexusName;
    }

    @Nonnull
    public Set<String> getTerminiNames() {
        return terminiNames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final WormholeNexus that = (WormholeNexus) o;

        return new EqualsBuilder().append(nexusName, that.nexusName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(nexusName).toHashCode();
    }
}
