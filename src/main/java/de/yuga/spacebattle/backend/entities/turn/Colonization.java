package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Colonization.getAll", query = "SELECT p FROM Colonization p"),
        @NamedQuery(name = "Colonization.getAllForUser", query = "SELECT p FROM Colonization p WHERE p.user = :user")
})
@Entity
@Table(name = "colonization")
@Check(constraints = "idPlanet is not null AND idUser is not null")
@AttributeOverride(name = "id", column = @Column(name = "idColonization"))
public class Colonization extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUser", updatable = false)
    private User user;

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idPlanet", updatable = false)
    private Planet planet;

    /**
     * Principle: Countdown to zero -> job done.
     * It's about full ticks
     */
    private int doneAtZero;

    public Colonization() {
    }

    public Colonization(@Nonnull final User user,
                        @Nonnull final Planet target,
                        final int doneAtZero) {
        Preconditions.checkNotNull(user, "fleet shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.user = user;
        this.planet = target;
        this.doneAtZero = doneAtZero;
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
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(@Nonnull final Planet planet) {
        this.planet = planet;
    }

    public int getDoneAtZero() {
        return doneAtZero;
    }

    public void setDoneAtZero(final int moveDoneAtZero) {
        if (moveDoneAtZero >= this.doneAtZero) {
            throw new NotifySBUserException("You cannot increase the traffic time until you have warp scrambler");
        }
        this.doneAtZero = moveDoneAtZero;
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
