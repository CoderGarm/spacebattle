package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

@Embeddable
public class OrbitalModuleJobElement {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idOrbitalModule")
    private OrbitalModule orbitalModule;

    @Min(1)
    private int amount;

    public OrbitalModuleJobElement() {
    }

    public OrbitalModuleJobElement(@Nonnull final OrbitalModule orbitalModule, final int amount) {
        Preconditions.checkNotNull(orbitalModule, "orbitalModule must not be empty");

        this.orbitalModule = orbitalModule;
        this.amount = amount;
    }

    @Nonnull
    public OrbitalModule getOrbitalModule() {
        return orbitalModule;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final OrbitalModuleJobElement that = (OrbitalModuleJobElement) o;

        return new EqualsBuilder().append(orbitalModule, that.orbitalModule).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(orbitalModule).toHashCode();
    }
}
