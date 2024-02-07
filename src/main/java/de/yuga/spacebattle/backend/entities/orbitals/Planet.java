package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EPlanetClassType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Planet.getAll", query = "SELECT p FROM Planet p"),
        @NamedQuery(name = "Planet.getAllOwned", query = "SELECT p FROM Planet p WHERE p.owner IS NOT NULL AND p.owner.dType = de.yuga.spacebattle.backend.enums.OwnerType.USER"),
        @NamedQuery(name = "Planet.getAllOwnedBy", query = "SELECT p FROM Planet p WHERE p.owner.id = :idOwner ORDER BY p.colonizedAt"),
        @NamedQuery(name = "Planet.getPlanetsWithBuildingsForResourceType",
                query = "SELECT p FROM Planet p LEFT JOIN FETCH p.constructions c WHERE p.owner.id = :idUser AND c.building.productionType.productionTarget = :resourceType"),
        @NamedQuery(name = "Planet.getMainPlanet", query = "SELECT p FROM Planet p WHERE p.owner.id = :idUser AND p.isMain = true"),
        @NamedQuery(name = "Planet.getByCoordinates", query = "SELECT p FROM Planet p WHERE p.system.id = :idStarSystem AND p.orbit.xCoordinate = :xCoordinate AND p.orbit.yCoordinate = :yCoordinate"),
})
@Entity
@Table(name = "planet",
        uniqueConstraints =
        @UniqueConstraint(name = "PLANET_UK", columnNames = {"idStarSystem", "idPlanet", "xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idPlanet"))
public class Planet extends AbstractEntityKey implements HasOwner {

    @Nonnull
    @Transient
    public static final EDistanceMetric PLANET_STANDARD_METRIC = EDistanceMetric.LS;

    @Nullable
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "idOwner")
    private Owner owner;

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
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idMiningFactors", nullable = false)
    private final MiningFactors miningFactors = new MiningFactors();

    /**
     * The amount of resources at this planet.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = ResourceDepositInitializerCalculator.initializeResourceDeposit();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceTransportationDemand", updatable = false)
    private final ResourceDeposit resourceTransportationDemand = new ResourceDeposit(EDepositType.TRANSPORTATION_DEMAND);

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceTransportationDelivery", updatable = false)
    private final ResourceDeposit resourceTransportationDelivery = new ResourceDeposit(EDepositType.TRANSPORTATION_DELIVERY);

    @Nonnull
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "planet")
    private final Set<Construction> constructions = new HashSet<>();

    @Nullable
    private LocalDateTime colonizedAt;

    /**
     * Marks if the planet is the main of the owner.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isMain;

    @Nonnull
    @Transient
    private final EPlanetClassType planetType = EPlanetClassType.PLANET;

    public Planet() {
    }

    public Planet(@Nonnull final String name,
                  @Nonnull final StarSystem system,
                  @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.colonizedAt = owner != null ? LocalDateTime.now() : null;
        this.name = name;
        this.system = system;
        this.orbit = orbit;
    }

    @Nullable
    @Override
    public Owner getOwner() {
        return owner;
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

    public void setOwner(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        colonizedAt = LocalDateTime.now();
        this.owner = owner;
        this.isMain = false;
    }

    @Nonnull
    public ResourceDeposit getResourceDeposit() {
        return resourceDeposit;
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

    public boolean isMain() {
        return isMain;
    }

    public void toggleMain() {
        isMain = true;
    }

    @Nonnull
    public EPlanetClassType getPlanetType() {
        return planetType;
    }

    @Nonnull
    public Set<Construction> getConstructions() {
        return constructions;
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
     * Checks if the planet is colonizable.
     *
     * @return <code>true</code> if the planet is colonizable, <code>false</code> otherwise
     */
    public boolean isColonizable() {
        return owner == null;
    }

    @Nonnull
    public ResourceDeposit getResourceTransportationDemand() {
        return resourceTransportationDemand;
    }

    @Nonnull
    public ResourceDeposit getResourceTransportationDelivery() {
        return resourceTransportationDelivery;
    }
}
