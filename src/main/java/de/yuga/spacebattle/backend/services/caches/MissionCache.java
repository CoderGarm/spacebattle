package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.EMissionActions;
import de.yuga.spacebattle.backend.enums.EMissionType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class MissionCache {

    @Nonnull
    private final CacheStore<String, List<MissionItem>> cache = new CacheStore<>(10, TimeUnit.DAYS);

    public void pirateRaidSpawn(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.SPAWN);
        addItem(today, target, missionItem);
    }

    public void pirateRaidApproach(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.APPROACH);
        addItem(today, target, missionItem);
    }

    public void pirateRaidWithdraw(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.WITHDRAW);
        addItem(today, target, missionItem);
    }

    public void pirateRaidWithdrawFromOrbit(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.LEAVE_ORBIT);
        addItem(today, target, missionItem);
    }

    public void pirateRaidBattleResult(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target, @Nullable final BattleReport battleReport, final boolean userDefeated) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        if (battleReport == null) {
            final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.NO_BATTLE);
            addItem(today, target, missionItem);
            return;
        }

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.BATTLE);
        missionItem.setUserDefeated(userDefeated);
        missionItem.setBattleReport(battleReport);
        addItem(today, target, missionItem);
    }

    public void pirateRaidTargetRaided(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionActions.RAID);
        addItem(today, target, missionItem);
    }

    @Nonnull
    public List<MissionItem> get(@Nonnull final Tick today, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        return Objects.requireNonNullElse(cache.get(getKey(today, target)), new ArrayList<>());
    }

    private void addItem(@Nonnull final Tick today, @Nonnull final Planet target, @Nonnull final MissionItem missionItem) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");
        Preconditions.checkNotNull(missionItem, "missionItem must not be empty");

        final String key = getKey(today, target);
        List<MissionItem> missionItems = cache.get(key);
        if (missionItems == null) {
            missionItems = new ArrayList<>();
        }
        missionItems.add(missionItem);
        cache.put(key, missionItems);
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");
        Preconditions.checkNotNull(target.getOwner(), "target.getOwner() must not be empty");

        return today.getNo() + "|" + target.getOwner().getId();

    }
}
