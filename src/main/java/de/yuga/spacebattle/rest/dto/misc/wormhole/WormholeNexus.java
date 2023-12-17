package de.yuga.spacebattle.rest.dto.misc.wormhole;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.space.EWormhole;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
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

    public boolean areSystemsConnected(@Nonnull final StarSystem o1, @Nonnull final StarSystem o2) {
        Preconditions.checkNotNull(o1, "o1 must not be empty");
        Preconditions.checkNotNull(o2, "o2 must not be empty");

        final String o1Name = o1.getName();
        final String o2Name = o2.getName();
        final Set<String> names = new HashSet<>(terminiNames);
        names.add(nexusName);

        if (nexusName.equals(EWormhole.CONGO.getWormhole().getNexusName()) || nexusName.equals(EWormhole.FELIX.getWormhole().getNexusName())) {
            // SGC-902-36-G Wormhole Anomaly
            names.addAll(EWormhole.CONGO.getWormhole().getTerminiNames());
            names.addAll(EWormhole.FELIX.getWormhole().getTerminiNames());
        }

        return names.contains(o1Name) && names.contains(o2Name);
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
