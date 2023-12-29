package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.misc.PointsCompletable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EJobPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Job.getAllByOwner", query = "SELECT j FROM Job j WHERE j.isDeleted = false AND j.owner.id = :idUser"),
        @NamedQuery(name = "Job.getAllForPlanet", query = "SELECT j FROM Job j WHERE j.isDeleted = false AND j.facility.planet.id = :idPlanet")
})
@Entity
@Table(name = "job")
@AttributeOverride(name = "id", column = @Column(name = "idJob"))
/* todo replace by validation necessary?
    @Check(constraints = "(idBuilding IS NOT NULL AND targetLevel IS NOT NULL) " +
        "OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) " +
        "OR (idFleet IS NOT NULL OR idFleetSnapshot IS NOT NULL) ")*/
public class Job extends PointsCompletable implements Comparable<Job>, HasOwner {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idFacility")
    private Construction facility;

    @Nonnull
    @Embedded
    private Constructable constructable;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EJobPriority priority = EJobPriority.NONE;

    public Job() {
    }

    /**
     * Unhappy with the facility-hack in case of a research.
     *
     * @param planet        the job's planet
     * @param facility      the facility if the job is located
     * @param constructable to job's content
     */
    public Job(@Nonnull final Planet planet,
               @Nonnull final Construction facility,
               @Nonnull final Constructable constructable,
               final boolean tickByPlanetsResources) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");
        Preconditions.checkArgument(planet.getHumanOwner() != null, "planet must be colonized!");

        this.owner = planet.getHumanOwner();
        this.facility = facility;
        this.constructable = constructable;
        this.pointsLeft = (int) constructable.getJobCosts().getResourceAmountByType(constructable.getResourceType());

        if (tickByPlanetsResources) {
            final long presentPoints = planet.getResourceDeposit().getResourceAmountByType(constructable.getResourceType());
            final long usedPoints = tick(presentPoints);
            planet.getResourceDeposit().updateResource(constructable.getResourceType(), -usedPoints);
        }
    }

    public void setOwner(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }


    @Nonnull
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

    @Nonnull
    public EJobPriority getPriority() {
        return priority;
    }

    public boolean matchesPriority(@Nonnull final EJobPriority priority) {
        Preconditions.checkNotNull(priority, "priority must not be empty");

        return this.priority == priority;
    }


    public void setPriority(@Nonnull final EJobPriority priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(@Nonnull final Job o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        if (matchesPriority(EJobPriority.PRIORITY) && !o.matchesPriority(EJobPriority.PRIORITY)) {
            return -1;
        }

        if (o.matchesPriority(EJobPriority.PRIORITY) && !matchesPriority(EJobPriority.PRIORITY)) {
            return 1;
        }

        return Long.compare(getPointsLeft(), o.getPointsLeft());
    }

    public boolean isFinished() {
        return pointsLeft <= 0;
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
