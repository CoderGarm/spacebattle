package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.calculator.resource.TickOutputCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "Planet.getAll", query = "SELECT p FROM Planet p"),
        @NamedQuery(name = "Planet.getAllOwned", query = "SELECT p FROM Planet p WHERE p.owner IS NOT NULL"),
        @NamedQuery(name = "Planet.getAllOwnedBy", query = "SELECT p FROM Planet p WHERE p.owner.id = :idOwner ORDER BY p.colonizedAt"),
        @NamedQuery(name = "Planet.getPlanetsWithBuildingsForResourceType",
                query = "SELECT p FROM Planet p LEFT JOIN FETCH p.constructions c WHERE p.owner = :owner AND c.building.productionType.productionTarget = :resourceType"),
        @NamedQuery(name = "Planet.getMainPlanet", query = "SELECT p FROM Planet p WHERE p.owner.id = :idUser GROUP BY p.colonizedAt"),
        @NamedQuery(name = "Planet.getByCoordinates", query = "SELECT p FROM Planet p WHERE p.system.id = :idStarSystem AND p.orbit.xCoordinate = :xCoordinate AND p.orbit.yCoordinate = :yCoordinate"),
})
@Entity
@Table(name = "planet",
        uniqueConstraints =
        @UniqueConstraint(name = "PLANET_UK", columnNames = {"idStarSystem", "idPlanet", "xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idPlanet"))
public class Planet extends AbstractEntityKey {

    @Nonnull
    @Transient
    public static final EDistanceMetric PLANET_STANDARD_METRIC = EDistanceMetric.LS;

    @Nullable
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "idOwner")
    private User owner;

    @Nonnull
    @NotNull
    @Size(min = 1, max = 30)
    private String name;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idStarSystem", updatable = false)
    private StarSystem system;

    @Embedded
    private Orbit orbit;

    /**
     * Describes the mining factors for every resource.
     */
    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idMiningFactors", nullable = false)
    private final MiningFactors miningFactors = new MiningFactors();

    /**
     * The amount of resources at this planet.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = ResourceDepositInitializerCalculator.initializeResourceDeposit(Planet.class, EDepositType.DEPOSITS);

    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "planet")
    private final Set<Construction> constructions = new HashSet<>();

    @Nullable
    private LocalDateTime colonizedAt;

    @Nonnull
    @Transient
    private final EPlanetClassType planetType = EPlanetClassType.PLANET;

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
        this.colonizedAt = owner != null ? LocalDateTime.now() : null;
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

        colonizedAt = LocalDateTime.now();
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
    public MiningFactors getMiningFactors() {
        return miningFactors;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    @Nullable
    public LocalDateTime getColonizedAt() {
        return colonizedAt;
    }

    @Nonnull
    public EPlanetClassType getPlanetType() {
        return planetType;
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
     * Returns the facility which produces the resource type.
     *
     * @param resourceType the requested resource type
     * @return the facility
     */
    @Nonnull
    public Set<Construction> getConstructionByResource(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == resourceType)
                .collect(Collectors.toSet());
    }

    /**
     * Checks the the planet is colonizable.
     *
     * @return <code>true</code> if the planet is colonizable, <code>false</code> otherwise
     */
    public boolean isColonizable() {
        return owner == null;
    }

    /**
     * Checks if this planet has a building for this resource type.
     *
     * @param resourceType the resource type
     * @return <code>true</code> if this planet has a construction for this type, <code>false</code> otherwise
     */
    public boolean hasProductionTarget(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getConstructions().stream().anyMatch(c -> resourceType == c.getBuilding().getProductionTarget());
    }

    /**
     * Returns the population capacity of this planet.
     *
     * @return the maximum capacity
     */
    public long getPopulationCapacity() {
        return getConstructionByResource(EResourceType.POPULATION).stream()
                .filter(c -> EProductionCategory.CAPACITY == c.getBuilding().getProductionType().getProductionCategory())
                .map(TickOutputCalculator::getTickOutputByLevelForPopulation).reduce(BigDecimal.ZERO, BigDecimal::add).longValue();
    }
}
