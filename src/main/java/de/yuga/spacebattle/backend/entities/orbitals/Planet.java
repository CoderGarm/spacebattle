package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@NamedQueries({
        @NamedQuery(name = "Planet.getAll", query = "SELECT p FROM Planet p"),
        @NamedQuery(name = "Planet.getAllOwned", query = "SELECT p FROM Planet p WHERE p.owner IS NOT NULL"),
        @NamedQuery(name = "Planet.getAllOwnedBy", query = "SELECT p FROM Planet p WHERE p.owner = :owner ORDER BY p.id")
})
@Entity
@Table(name = "planet",
        uniqueConstraints =
        @UniqueConstraint(name = "PLANET_UK", columnNames = {"idStarSystem", "idPlanet", "xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idPlanet"))
public class Planet extends AbstractEntityKey {

    @Nullable
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "idOwner")
    private User owner;

    @Nonnull
    @NotNull(message = "name must not be null")
    @Size(min = 1, max = 30)
    private String name;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idStarSystem", updatable = false)
    private StarSystem system;

    @Embedded
    private Orbit orbit;

    /**
     * Describes the mining factors for every rescource.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceFactor", updatable = false)
    private final ResourceDeposit resourceFactors = new ResourceDeposit(EResourceSubType.MININGFACTORS);

    /**
     * The amount of resources at this planet.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = new ResourceDeposit(EResourceSubType.DEPOSITS);

    @Nonnull
    @OneToMany(mappedBy = "planet", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final Set<Construction> constructions = new HashSet<>();

    public Planet() {
    }

    public Planet(@Nullable final User owner,
                  @Nonnull final String name,
                  @Nonnull final StarSystem system,
                  @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.owner = owner;
        this.name = name;
        this.system = system;
        this.orbit = orbit;

    }

    @Nullable
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nonnull
    public ResourceDeposit getResourceDeposit() {
        return resourceDeposit;
    }

    @Nonnull
    public Set<Construction> getConstructions() {
        return constructions;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    public StarSystem getSystem() {
        return system;
    }

    @Nonnull
    public ResourceDeposit getResourceFactors() {
        return resourceFactors;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Planet)) return false;

        Planet planet = (Planet) o;

        return id == planet.id;
    }

    @Override
    public int hashCode() {
        return id * 7;
    }

    /**
     * Returns the tickly output of this planet for a specific resource type.
     *
     * @param resourceType the resource type which should be calculated
     * @return the effective tickly output
     */
    @Nonnull
    public BigDecimal getTickOutputForResourceType(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        final AtomicReference<BigDecimal> output = new AtomicReference<>();
        output.set(BigDecimal.ZERO);
        final BigDecimal resourceAmountByType = getResourceFactors().getResourceAmountByType(resourceType);
        getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == resourceType)
                .findFirst()
                .ifPresent(construction -> {
                    output.set(construction.getTickOutput(resourceAmountByType));
                });

        return output.get();
    }

    /**
     * Returns the facility which produces the resource type.
     *
     * @param resourceType the requested resource type
     * @return the facility
     */
    @Nullable
    public Construction getConstructionByResource(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == resourceType)
                .findFirst().orElse(null);
    }
}
