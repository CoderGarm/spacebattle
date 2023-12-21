package de.yuga.spacebattle.backend.entities.turn.navigation;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Move;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "flightPlan")
@AttributeOverride(name = "id", column = @Column(name = "idFlightPlan"))
@DiscriminatorValue("FLIGHT_PLAN")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dType", discriminatorType = DiscriminatorType.STRING)
public class FlightPlan extends AbstractEntityKey {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idMove", updatable = false)
    private Move move;

    @OrderColumn
    @Column(columnDefinition = "decimal(3, 0)")
    private int timeAfterStart;

    @Nonnull
    @Embedded
    private FleetOrbit location;

    public FlightPlan() {
    }

    public FlightPlan(@Nonnull final FleetOrbit location, final int timeAfterStart) {
        this.location = Preconditions.checkNotNull(location, "location must not be empty");
        this.timeAfterStart = timeAfterStart;
    }

    public FlightPlan(@Nonnull final FlightPlan flightPlan, @Nonnull final Move move) {
        Preconditions.checkNotNull(flightPlan, "flightPlan must not be empty");
        this.move = Preconditions.checkNotNull(move, "move must not be empty");
        this.location = flightPlan.getLocation();
        this.timeAfterStart = flightPlan.getTimeAfterStart();
    }

    public void setMove(@Nonnull final Move move) {
        this.move = Preconditions.checkNotNull(move, "move must not be empty");
    }

    @Nonnull
    public Move getMove() {
        return move;
    }

    public int getTimeAfterStart() {
        return timeAfterStart;
    }

    @Nonnull
    public FleetOrbit getLocation() {
        return location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FlightPlan that = (FlightPlan) o;

        return new EqualsBuilder().append(move, that.move).append(timeAfterStart, that.timeAfterStart).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(move).append(timeAfterStart).toHashCode();
    }
}
