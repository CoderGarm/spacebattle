package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Job.getAll", query = "SELECT p FROM Job p"),
        @NamedQuery(name = "Job.getAllByOwner", query = "SELECT p FROM Job p WHERE p.owner = :owner")
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

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idFacility")
    private Construction facility;

    @Nonnull
    @Embedded
    private Constructable constructable;

    /**
     * Principle: Countdown to zero -> job done.
     * "Construction points based" (construction yard, shipyard or laboratories) for
     * {@link EResourceType#CONSTRUCTION}
     * {@link EResourceType#ORBITALCONSTRUCTION}
     * {@link EResourceType#RESEARCH}
     */
    @Nonnull
    @NotNull
    private BigDecimal jobDoneAtZero = BigDecimal.TEN.movePointRight(10);

    public Job() {
    }

    /**
     * Unhappy with the facility-hack in case of a research.
     *
     * @param owner         the job's owner
     * @param facility      the facility if the job is locatable
     * @param constructable to job's content
     */
    public Job(@Nonnull User owner, @Nonnull Construction facility, @Nonnull Constructable constructable) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");

        this.owner = owner;
        this.facility = facility;
        this.constructable = constructable;
        this.jobDoneAtZero = this.constructable.getJobCosts().get(constructable.getResourceType());
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nullable
    public Construction getFacility() {
        return facility;
    }

    public void setFacility(@Nullable final Construction facility) {
        this.facility = facility;
    }

    @Nonnull
    public Constructable getConstructable() {
        return constructable;
    }

    public void setConstructable(@Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");

        this.constructable = constructable;
    }

    @Nonnull
    public BigDecimal getJobDoneAtZero() {
        return jobDoneAtZero;
    }

    public void setJobDoneAtZero(@Nonnull final BigDecimal jobDoneAtZero) {
        Preconditions.checkNotNull(jobDoneAtZero, "jobDoneAtZero shouldn't be null!");

        this.jobDoneAtZero = jobDoneAtZero;
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
