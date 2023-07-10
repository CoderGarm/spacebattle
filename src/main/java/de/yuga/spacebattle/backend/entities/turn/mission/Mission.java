package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "mission")
@AttributeOverride(name = "id", column = @Column(name = "idMission"))
@DiscriminatorColumn(name = "missionType", discriminatorType = DiscriminatorType.STRING)
public class Mission extends Deletable {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idActor", updatable = false)
    private User actor;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickStartedAt")
    private Tick started;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickStoppedAt")
    private Tick stopped;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private EMissionType missionType;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idMission")
    private final Set<WarShip> ships = new HashSet<>();

    /**
     * The place to be.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPlanet")
    private Planet venue;

    public Mission() {
    }

    public Mission(@Nonnull final User actor,
                   @Nonnull final Tick started,
                   @Nonnull final Planet venue) {
        this.actor = Preconditions.checkNotNull(actor, "actor must not be empty");
        this.started = Preconditions.checkNotNull(started, "started must not be empty");
        this.venue = Preconditions.checkNotNull(venue, "venue must not be empty");
    }

    @Nonnull
    public User getActor() {
        return actor;
    }

    @Nonnull
    public Tick getStarted() {
        return started;
    }

    @Nullable
    public Tick getStopped() {
        return stopped;
    }

    public void setStopped(@Nullable final Tick stopped) {
        this.stopped = stopped;
        this.delete();
    }

    @Nonnull
    public Set<WarShip> getShips() {
        return ships;
    }

    @Nonnull
    public Planet getVenue() {
        return venue;
    }

    @Nonnull
    public EMissionType getMissionType() {
        return missionType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Mission mission = (Mission) o;

        return new EqualsBuilder().append(id, mission.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    public void enrichWithShips(@Nonnull final List<WarShip> warShips) {
        this.ships.addAll(Preconditions.checkNotNull(warShips, "warShips must not be empty"));
    }
}
