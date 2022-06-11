package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Job.getAll", query = "SELECT p FROM Job p"),
        @NamedQuery(name = "Job.getAllByOwner", query = "SELECT p FROM Job p WHERE p.owner = :owner"),
        @NamedQuery(name = "Job.getAllForConstruction", query = "SELECT p FROM Job p WHERE p.facility = :facility"),
        @NamedQuery(name = "Job.isPresentForResearch", query = "SELECT CASE WHEN (COUNT(p) > 0)  THEN TRUE ELSE FALSE END FROM Job p WHERE p.constructable.research = :research"),
        @NamedQuery(name = "Job.isPresentForResearches", query = "SELECT new de.yuga.spacebattle.backend.entities.researches.ActiveResearchTuple(p.constructable.research, CASE WHEN (COUNT(p) > 0)  THEN TRUE ELSE FALSE END) FROM Job p WHERE p.constructable.research IN (:researches)"),
        @NamedQuery(name = "Job.getAllForPlanet", query = "SELECT p FROM Job p WHERE p.facility.planet.id = :idPlanet")
})
@Entity
@Table(name = "job")
@AttributeOverride(name = "id", column = @Column(name = "idJob"))
@Check(constraints = "(idBuilding IS NOT NULL AND targetLevel IS NOT NULL) " +
        "OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) " +
        "OR (idShipClass IS NOT NULL AND amountShips IS NOT NULL)")
public class Job extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner", updatable = false)
    private User owner;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idFacility")
    private Construction facility;

    @Nonnull
    @Embedded
    private Constructable constructable;

    /**
     * Principle: Countdown ticks to zero -> job done.<br>
     * Explanation change to tick: You cannot improve the production while you build a constructable.<br>
     * "Construction points based" (construction yard, shipyard or laboratories) for<br>
     * {@link EResourceType#CONSTRUCTION}<br>
     * {@link EResourceType#ORBITAL_CONSTRUCTION}<br>
     * {@link EResourceType#RESEARCH}
     */
    @NotNull
    @Column(columnDefinition = "decimal(19, 0)")
    private int jobDoneAtZero;

    public Job() {
    }

    /**
     * Unhappy with the facility-hack in case of a research.
     *
     * @param planet        the job's planet
     * @param facility      the facility if the job is located
     * @param constructable to job's content
     */
    public Job(@Nonnull final Planet planet, @Nonnull final Construction facility, @Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");
        Preconditions.checkArgument(planet.getOwner() != null, "planet must be colonized!");

        this.owner = planet.getOwner();
        this.facility = facility;
        this.constructable = constructable;
        this.jobDoneAtZero = JobCostsCalculator.calculateRemainingTicks(facility, constructable);
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nonnull
    public Construction getFacility() {
        return facility;
    }

    @Nonnull
    public Constructable getConstructable() {
        return constructable;
    }

    public void setConstructable(@Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");

        this.constructable = constructable;
    }

    public long getJobDoneAtZero() {
        return jobDoneAtZero;
    }

    /**
     * A job is done at zero and needed to be counted down.
     */
    public void tick() {
        this.jobDoneAtZero--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;

        Job job = (Job) o;

        return id == job.id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
