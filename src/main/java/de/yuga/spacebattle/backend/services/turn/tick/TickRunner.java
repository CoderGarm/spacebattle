package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.List;

public interface TickRunner extends Comparable<TickRunner> {

    List<Class<? extends TickRunner>> SEQUENCE_OF_RUNNERS = List.of(
            TickPreparationRunner.class,
            NPCFleetTickRunner.class,
            EmpireTransportationTickRunner.class,
            EmpireMigrationTickRunner.class,
            TradeTickRunner.class,
            PlanetTickRunner.class,
            ResearchTickRunner.class,
            OperationalTickRunner.class,
            FleetMovementTickRunner.class,
            ColonizationTickRunner.class,
            NPCMissionRunner.class,
            UserBattleRunner.class,
            HeatMapRunner.class,
            HousekeepingRunner.class,
            TickAdviceEMailRunner.class
    );

    /**
     * The handler method.
     */
    void tick(@Nonnull Tick today);

    @Override
    default int compareTo(@Nonnull final TickRunner o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        final int i = SEQUENCE_OF_RUNNERS.indexOf(this.getClass());
        final int i1 = SEQUENCE_OF_RUNNERS.indexOf(o.getClass());
        if (i == -1 || i1 == -1 || i == i1) {
            throw new NotImplementedException("You missed to sequencing a service, boy!");
        }
        //noinspection ComparatorMethodParameterNotUsed
        return Integer.compare(i, i1);
    }

    static boolean isActive(@Nonnull final TickRunner toCheck) {
        Preconditions.checkNotNull(toCheck, "toCheck must not be empty");

        return SEQUENCE_OF_RUNNERS.stream().anyMatch(runner -> runner.isAssignableFrom(toCheck.getClass()));
    }
}
