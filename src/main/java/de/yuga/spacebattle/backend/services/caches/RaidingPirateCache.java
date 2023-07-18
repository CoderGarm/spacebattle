package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RaidingPirateCache {

    @Nonnull
    private final CacheStore<Fleet, List<EMissionAction>> doNotMoveCache = new CacheStore<>(5, TimeUnit.DAYS);

    @Nonnull
    private final CacheStore<Fleet, Planet> targetCache = new CacheStore<>(10, TimeUnit.DAYS);

    public void executeNext(@Nonnull final Fleet pirateFleet, @Nonnull final EMissionAction... missionAction) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(missionAction, "missionAction must not be empty");

        doNotMoveCache.put(pirateFleet, Arrays.asList(missionAction));
    }

    public void dropFirstActionItem(@Nonnull final Fleet pirateFleet, @Nonnull final EMissionAction missionAction) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(missionAction, "missionAction must not be empty");

        List<EMissionAction> eMissionActions = doNotMoveCache.get(pirateFleet);
        if (eMissionActions != null && missionAction == eMissionActions.get(0)) {
            eMissionActions = new ArrayList<>(eMissionActions);
            eMissionActions.remove(0);
            doNotMoveCache.put(pirateFleet, eMissionActions);
        }
    }

    @Nonnull
    public List<EMissionAction> getNextActions(@Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        final List<EMissionAction> eMissionActions = doNotMoveCache.get(pirateFleet);
        return eMissionActions != null ? eMissionActions : List.of();
    }

    public boolean isPhaseSequenceValid(@Nonnull final Fleet pirateFleet,
                                        @Nonnull final EMissionAction... actions) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(actions, "actions must not be empty");

        final List<EMissionAction> nextActions = getNextActions(pirateFleet);
        for (int i = 0; i < actions.length; i++) {
            if (nextActions.size() - 1 < i) {
                return false;
            }
            if (nextActions.get(i) != actions[i]) {
                return false;
            }
        }
        return true;
    }

    public void setTarget(@Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        targetCache.put(pirateFleet, target);
    }

    @Nullable
    public Planet getTarget(@Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        return targetCache.get(pirateFleet);
    }
}
