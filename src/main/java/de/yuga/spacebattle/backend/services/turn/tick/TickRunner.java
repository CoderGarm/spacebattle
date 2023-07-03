package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.List;

public interface TickRunner extends Comparable<TickRunner> {

    /**
     * The handler method.
     */
    void tick(@Nonnull Tick today);

    @Override
    default int compareTo(@Nonnull final TickRunner o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        final List<Class<? extends TickRunner>> sequenceOfRunners = List.of(
                NPCFleetTickRunner.class,
                EmpireTransportationTickRunner.class,
                EmpireMigrationTickRunner.class,
                TradeTickRunner.class,
                PlanetTickRunner.class,
                FleetMovementTickRunner.class,
                ColonizationTickRunner.class
        );

        final int i = sequenceOfRunners.indexOf(this.getClass());
        final int i1 = sequenceOfRunners.indexOf(o.getClass());
        if (i == -1 || i1 == -1 || i == i1) {
            throw new NotImplementedException("You missed to sequencing a service, boy!");
        }
        //noinspection ComparatorMethodParameterNotUsed
        return Integer.compare(i, i1);
    }
}
