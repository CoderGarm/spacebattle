package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MissionRandomizer {

    @Nonnull
    private static final Map<CombinedKey, List<EMissionAction>> NEXT_STEPS = Map.of(
            new CombinedKey(EMissionType.PIRATE_RAID, EMissionAction.SPAWN), List.of(EMissionAction.APPROACH, EMissionAction.WITHDRAW),
            new CombinedKey(EMissionType.PIRATE_RAID, EMissionAction.APPROACH), List.of(EMissionAction.BATTLE),
            new CombinedKey(EMissionType.PIRATE_RAID, EMissionAction.WITHDRAW), List.of(EMissionAction.END_OF_MISSION)
    );

    private MissionRandomizer() {
    }

    public static boolean shouldWait(@Nonnull final EMissionType missionType,
                                     @Nonnull final Fleet pirateFleet,
                                     @Nonnull final Tick today) {
        Preconditions.checkNotNull(missionType, "missionType must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");


        final boolean pirateEven = isEven(pirateFleet);
        final boolean tickEven = isEven(today);

        if (pirateEven && tickEven) {
            return true;
        }
        //noinspection ConstantValue
        if (pirateEven && !tickEven) {
            return false;
        }
        //noinspection ConstantValue
        if (!pirateEven && tickEven) {
            return true;
        }
        //noinspection ConstantValue,IfStatementWithIdenticalBranches
        if (!pirateEven && !tickEven) {
            return false;
        }
        return false;
    }

    @Nonnull
    public static List<EMissionAction> getMissionSequence(@Nonnull final EMissionType missionType,
                                                          @Nonnull final Fleet pirateFleet,
                                                          @Nonnull final Tick today) {
        Preconditions.checkNotNull(missionType, "missionType must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final List<EMissionAction> nextActions = new ArrayList<>();
        getMissionSequence(missionType, EMissionAction.SPAWN, pirateFleet, today, nextActions);
        EMissionAction nextAction = null;
        while (nextAction != EMissionAction.END_OF_MISSION) {
            nextAction = nextActions.get(nextActions.size() - 1);
            getMissionSequence(missionType, nextAction, pirateFleet, today, nextActions);
        }
        /* todo idea to randomize pirate action:
            - do not approach directly after spawn
            - do not withdraw after raid
            - approach another planet if cargo hold is empty
        */
        return nextActions;
    }

    @Nonnull
    public static List<EMissionAction> getMissionSequence(@Nonnull final EMissionType missionType,
                                                          @Nonnull final EMissionAction missionAction,
                                                          @Nonnull final Fleet pirateFleet,
                                                          @Nonnull final Tick today,
                                                          @Nonnull final List<EMissionAction> steps) {
        Preconditions.checkNotNull(missionType, "missionType must not be empty");
        Preconditions.checkNotNull(missionAction, "missionAction must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final List<EMissionAction> nextActions = NEXT_STEPS.get(new CombinedKey(missionType, missionAction));
        final EMissionAction nextAction = MissionRandomizer.getNextAction(nextActions, pirateFleet, today);

        steps.add(missionAction);
        steps.add(nextAction);
        return steps;
    }

    @Nullable
    private static EMissionAction getNextAction(@Nonnull final List<EMissionAction> nextActions,
                                                @Nonnull final Fleet pirateFleet,
                                                @Nonnull final Tick today) {
        Preconditions.checkNotNull(nextActions, "nextActions must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        if (nextActions.size() == 1) {
            return nextActions.get(0);
        }

        final int nexted = new Random().nextInt(nextActions.size() - 1);
        return nextActions.get(nexted);
    }

    // fixme kann das weg?
    private static int getOneOf(@Nonnull final List<Integer> possibleValues, @Nonnull final Fleet pirateFleet, @Nonnull final Tick today) {
        Preconditions.checkNotNull(possibleValues, "possibleValues must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final int size = possibleValues.size();
        final int lastIndex = size - 1;
        if (size == 1) {
            return possibleValues.get(lastIndex);
        }

        final int nexted = new Random().nextInt(lastIndex);
        return possibleValues.get(nexted);
/*
        final boolean pirateEven = isEven(pirateFleet);
        final boolean tickEven = isEven(today);

        if (pirateEven && tickEven) {
            // return last even indexed value
            return possibleValues.stream().filter(MissionRandomizer::isEven).reduce((o1, o2) -> o2).orElseGet(() -> possibleValues.get(0));
        }
        //noinspection ConstantValue
        if (pirateEven && !tickEven) {
            // return second odd indexed value
            return possibleValues.stream().filter(MissionRandomizer::isOdd).skip(1).findFirst().orElseGet(() -> possibleValues.get(lastIndex));
        }
        //noinspection ConstantValue
        if (!pirateEven && tickEven) {
            // return first even indexed value
            return possibleValues.stream().filter(MissionRandomizer::isEven).findFirst().orElseGet(() -> possibleValues.get(0));
        }
        //noinspection ConstantValue
        if (!pirateEven && !tickEven) {
            // return first odd indexed value
            return possibleValues.stream().filter(MissionRandomizer::isOdd).findFirst().orElseGet(() -> possibleValues.get(lastIndex));
        }
        throw new NotifyWebUserException("Not on my watch!");*/
    }

    private static boolean isOdd(@Nonnull final AbstractEntityKey entityKey) {
        return !MissionRandomizer.isEven(entityKey);
    }

    private static boolean isEven(@Nonnull final AbstractEntityKey entityKey) {
        Preconditions.checkNotNull(entityKey, "entityKey must not be empty");

        return isEven(entityKey.getId());
    }

    private static boolean isOdd(final int value) {
        return !isEven(value);
    }

    private static boolean isEven(final int value) {
        return value % 2 == 0;
    }

    private static class CombinedKey {

        @Nonnull
        private final EMissionType missionType;

        @Nonnull
        private final EMissionAction initialAction;

        public CombinedKey(@Nonnull final EMissionType missionType, @Nonnull final EMissionAction initialAction) {
            this.missionType = Preconditions.checkNotNull(missionType, "missionType must not be empty");
            this.initialAction = Preconditions.checkNotNull(initialAction, "initialAction must not be empty");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            final CombinedKey that = (CombinedKey) o;

            return new EqualsBuilder().append(missionType, that.missionType).append(initialAction, that.initialAction).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(missionType).append(initialAction).toHashCode();
        }
    }
}
