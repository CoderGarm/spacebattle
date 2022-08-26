package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * This describes a running colonization.
 */
@NamedQueries({
        @NamedQuery(name = "Colonization.getAll", query = "SELECT p FROM Colonization p"),
        @NamedQuery(name = "Colonization.getAllForUser", query = "SELECT p FROM Colonization p WHERE p.user.id = :idUser")
})
@Entity
@Table(name = "colonization")
@AttributeOverride(name = "id", column = @Column(name = "idColonization"))
public class Colonization extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUser", updatable = false, nullable = false)
    private User user;

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idTarget", updatable = false, nullable = false)
    private Planet target;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EDepositType.DEPOSITS);

    /**
     * Principle: Countdown ticks to zero -> job done.
     * It's about full ticks
     */
    private int doneAtZero;

    public Colonization() {
    }

    public Colonization(@Nonnull final User user,
                        @Nonnull final Planet target,
                        @Nonnull final CrewRequirement crewRequirement,
                        final int doneAtZero) {
        Preconditions.checkNotNull(user, "fleet shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.user = user;
        this.target = target;
        this.doneAtZero = doneAtZero;
        // do the switch because these are costs up to here but the running colonization knows all the people as deposit
        this.costs.updatePopulation(crewRequirement.toggleToDepositMode());
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    public void setUser(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.user = owner;
    }

    @Nonnull
    public Planet getTarget() {
        return target;
    }

    public void setTarget(@Nonnull final Planet planet) {
        this.target = planet;
    }

    public int getDoneAtZero() {
        return doneAtZero;
    }

    public void setDoneAtZero(final int moveDoneAtZero) {
        if (moveDoneAtZero >= this.doneAtZero) {
            throw new NotifyWebUserException("You cannot increase the traffic time until you have warp scrambler");
        }
        this.doneAtZero = moveDoneAtZero;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Colonization)) return false;

        Colonization that = (Colonization) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 37;
    }
}
