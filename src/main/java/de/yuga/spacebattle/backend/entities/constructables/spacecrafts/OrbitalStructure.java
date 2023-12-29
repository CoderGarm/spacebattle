package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "orbitalStructure")
@AttributeOverride(name = "id", column = @Column(name = "idOrbitalStructure"))
public class OrbitalStructure extends Operationable implements HasOwner {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @Embedded
    private FleetOrbit orbit;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOrbitalModule")
    private OrbitalModule module;

    @Min(1)
    private int amount;

    public OrbitalStructure() {
    }

    public OrbitalStructure(@Nonnull final Planet planet,
                            @Nonnull final OrbitalModule orbitalModule,
                            final int amount) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(orbitalModule, "orbitalModule must not be empty");
        Preconditions.checkNotNull(planet.getOwner(), "planet.getOwner() must not be empty");

        this.owner = planet.getOwner();
        this.orbit = new FleetOrbit(planet);
        this.module = orbitalModule;
        this.amount = amount;
    }

    @Nonnull
    @Override
    public Owner getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nullable
    @Override
    public User getHumanOwner() {
        if (!(owner instanceof User)) {
            return null;
        }
        return (User) owner;
    }

    @Nullable
    @Override
    public NonPlayerCharacter getNpcOwner() {
        if (!(owner instanceof NonPlayerCharacter)) {
            return null;
        }
        return (NonPlayerCharacter) owner;
    }

    @Nonnull
    public FleetOrbit getOrbit() {
        return orbit;
    }

    @Nonnull
    public OrbitalModule getModule() {
        return module;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final OrbitalStructure that = (OrbitalStructure) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        final ResourceDeposit result = new ResourceDeposit(EDepositType.COSTS);
        for (int i = 0; i < amount; i++) {
            result.add(module.getCosts());
        }
        return result;
    }
}
