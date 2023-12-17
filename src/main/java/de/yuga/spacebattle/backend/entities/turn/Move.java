package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.misc.Completable;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.navigation.FlightPlan;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.space.EWormhole;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "move")
@Check(constraints = "xCoordinateOrigin != xCoordinateDestination AND yCoordinateOrigin != yCoordinateDestination")
@AttributeOverride(name = "id", column = @Column(name = "idMove"))
public class Move extends Completable implements HasOwner {

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idUser", updatable = false)
    private Owner owner;

    @Nullable
    @OneToOne
    @JoinColumn(name = "idFleet")
    private Fleet fleet;

    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idFleetSnapshot")
    private FleetSnapshot fleetSnapshot;

    @Nonnull
    @NotNull
    @Embedded
    @AttributeOverride(name = "orbit.xCoordinate", column = @Column(name = "xCoordinateOrigin"))
    @AttributeOverride(name = "orbit.yCoordinate", column = @Column(name = "yCoordinateOrigin"))
    @AssociationOverride(name = "planet", joinColumns = @JoinColumn(name = "idPlanetOrigin"))
    @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "idStarSystemOrigin"))
    private FleetOrbit originOrbit;

    @Nonnull
    @NotNull
    @Embedded
    @AttributeOverride(name = "orbit.xCoordinate", column = @Column(name = "xCoordinateDestination"))
    @AttributeOverride(name = "orbit.yCoordinate", column = @Column(name = "yCoordinateDestination"))
    @AssociationOverride(name = "planet", joinColumns = @JoinColumn(name = "idPlanetDestination"))
    @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "idStarSystemDestination"))
    private FleetOrbit destinationOrbit;

    /**
     * The original duration without modification;
     */
    @Column(updatable = false)
    private int originalDuration;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickStarted", referencedColumnName = "idTick")
    private Tick started;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "move")
    private final List<FlightPlan> flightPlan = new ArrayList<>();

    @Nonnull
    @Transient
    private final List<StarSystem> waypoints = new ArrayList<>();

    public Move() {
    }

    public Move(@Nonnull final Tick today,
                @Nonnull final Fleet fleet,
                @Nonnull final FleetOrbit destination,
                @Nonnull final List<StarSystem> waypoints) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");
        Preconditions.checkNotNull(waypoints, "waypoints must not be empty");

        this.started = today;
        this.owner = fleet.getOwner();
        this.fleet = fleet;
        final FleetOrbit orbit = fleet.getOrbit();
        this.originOrbit = new FleetOrbit(orbit);
        this.destinationOrbit = destination;
        this.waypoints.addAll(waypoints);

        final boolean ftlCapable = fleet.isFTLCapable();
        final StarSystem originSystem = orbit.getSystem();
        final StarSystem destinationSystem = destination.getSystem();

        if (!ftlCapable && !StarSystem.equalsAtMap(originSystem, destinationSystem)) {
            // can not move
            this.ticksLeft = -1;
        } else {
            if (waypoints.size() > 2) {
                // origin and destination are always in
                calculateWaypoints(fleet, waypoints);
            } else {
                this.ticksLeft = DistanceCalculator.calculateTimeToTravel(fleet, destination);
            }
        }
        this.originalDuration = this.ticksLeft;
    }

    public Move(@Nonnull final Tick today,
                @Nonnull final Fleet fleet,
                @Nonnull final FleetOrbit destination,
                final int timeToTravel,
                final int originalDuration) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");

        this.started = today;
        this.owner = fleet.getOwner();
        this.fleet = fleet;
        this.originOrbit = new FleetOrbit(fleet.getOrbit());
        this.destinationOrbit = destination;
        this.ticksLeft = timeToTravel;
        this.originalDuration = originalDuration;
    }


    private void calculateWaypoints(@Nonnull final Fleet fleet,
                                    @Nonnull final List<StarSystem> waypoints) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");
        Preconditions.checkNotNull(waypoints, "waypoints must not be empty");

        double travelTime = 0;
        final ETechnologyType restrictingTechnologyType = fleet.getRestrictingTechnologyType();
        final Acceleration acceleration = fleet.getAccelerationFor(EModuleType.FTLPROPULSION);
        // way from inner-system to hyper limit will be ignored - it's a too small potion of the time tick-wise
        travelTime += DistanceCalculator.getSubLightDurationToHyperLimit(restrictingTechnologyType, acceleration, fleet.getOrbit());
        for (int i = 1; i < waypoints.size(); i++) {
            final StarSystem before = waypoints.get(i - 1);
            final StarSystem waypoint = waypoints.get(i);
            final Orbit beforeOrbit = before.getOrbit();
            final Orbit waypointOrbit = waypoint.getOrbit();
            final boolean systemsConnected = EWormhole.areSystemsConnected(before, waypoint);
            final double duration = systemsConnected ? 0.1 : DistanceCalculator.getDuration(EModuleType.FTLPROPULSION, restrictingTechnologyType, acceleration, beforeOrbit, waypointOrbit);
            final int steps = (int) (travelTime + duration) - (int) duration;
            if (steps >= 1) {  /* fixme something funny happens here */
                final List<Orbit> navPoints = DistanceCalculator.getWaypoints(EModuleType.FTLPROPULSION, restrictingTechnologyType, acceleration, beforeOrbit, waypointOrbit, steps);
                navPoints.forEach(n -> {
                    final int index = this.flightPlan.size() + navPoints.indexOf(n) + 1;
                    this.flightPlan.add(new FlightPlan(this, new FleetOrbit(n, null), index));
                });
            }
            travelTime += duration;
        }
        this.ticksLeft = (int) Math.ceil(travelTime);
    }

    @Nonnull
    public List<StarSystem> getWaypoints() {
        return waypoints;
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
    @SuppressWarnings("DataFlowIssue")
    public Fleet getFleet() {
        if (!isDeleted() && fleet == null) {
            throw new NotifyWebUserException("Hell no! This is not valid.");
        }
        return fleet;
    }

    @Nullable
    public FleetSnapshot getFleetSnapshot() {
        return fleetSnapshot;
    }

    @Nonnull
    public FleetOrbit getOriginOrbit() {
        return originOrbit;
    }

    @Nonnull
    public FleetOrbit getDestinationOrbit() {
        return destinationOrbit;
    }

    public void setTicksLeft(final int moveDoneAtZero) {
        if (moveDoneAtZero >= this.ticksLeft) {
            throw new NotifyWebUserException("You cannot increase the traffic time until you have warp scrambler");
        }
        this.ticksLeft = moveDoneAtZero;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    /**
     * Checks if this move is between the stars.
     *
     * @return <code>true</code> if this move is between stars, <code>false</code> otherwise
     */
    public boolean isInterstellarTravel() {
        return originOrbit.getSystem() == null || destinationOrbit.getSystem() == null || !originOrbit.getSystem().equals(destinationOrbit.getSystem());
    }

    @Override
    public void setFinished(@Nonnull final Tick finishedAt) {
        Preconditions.checkNotNull(finishedAt, "finishedAt must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        fleetSnapshot = new FleetSnapshot(fleet, fleet.getAliveShips());
        fleet = null;
        super.setFinished(finishedAt);
    }

    @Nonnull
    public Tick getStarted() {
        return started;
    }

    @Nonnull
    public List<FlightPlan> getFlightPlan() {
        return flightPlan;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Move move = (Move) o;

        return new EqualsBuilder().append(fleet, move.fleet).append(fleetSnapshot, move.fleetSnapshot).append(originOrbit, move.originOrbit).append(destinationOrbit, move.destinationOrbit).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(fleet).append(fleetSnapshot).append(originOrbit).append(destinationOrbit).toHashCode();
    }
}
