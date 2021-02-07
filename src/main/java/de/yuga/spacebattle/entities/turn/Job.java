package de.yuga.spacebattle.entities.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.Constructable;
import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Job.getAll", query = "SELECT p FROM Job p"),
        @NamedQuery(name = "Job.getAllByOwner", query = "SELECT p FROM Job p WHERE p.owner = :owner")
})
@Entity
@Table(name = "job", uniqueConstraints = @UniqueConstraint(columnNames = "idFacility"))
@AttributeOverride(name = "id", column = @Column(name = "idJob"))
public class Job extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idUser", updatable = false)
    private User owner;

    @JsonIgnore
    @Nonnull
    @OneToOne//(cascade = CascadeType.ALL) // todo detached entity problem
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

    public Job(@Nonnull User owner, @Nonnull Construction facility, @Nonnull Constructable constructable) {
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

    @Nonnull
    public Construction getFacility() {
        return facility;
    }

    public void setFacility(@Nonnull final Construction facility) {
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");

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

    public void setJobDoneAtZero(@Nonnull BigDecimal jobDoneAtZero) {
        Preconditions.checkNotNull(jobDoneAtZero, "jobDoneAtZero shouldn't be null!");

        this.jobDoneAtZero = jobDoneAtZero;
    }
}
