package de.yuga.spacebattle.backend.dto.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MissionItem {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Fleet pirateFleet;

    @Nonnull
    private final Planet target;

    @Nonnull
    private final EMissionType eMissionType;

    @Nonnull
    private final EMissionAction missionAction;

    @Nullable
    private Boolean userDefeated;

    @Nullable
    private BattleReport battleReport;

    public MissionItem(@Nonnull final Tick today,
                       @Nonnull final Fleet pirateFleet,
                       @Nonnull final Planet target,
                       @Nonnull final EMissionType eMissionType,
                       @Nonnull final EMissionAction missionAction) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");
        this.pirateFleet = Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        this.target = Preconditions.checkNotNull(target, "target must not be empty");
        this.eMissionType = Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");
        this.missionAction = Preconditions.checkNotNull(missionAction, "eMissionActions must not be empty");
    }

    public void setUserDefeated(@Nullable final Boolean userDefeated) {
        this.userDefeated = userDefeated;
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Fleet getPirateFleet() {
        return pirateFleet;
    }

    @Nonnull
    public Planet getTarget() {
        return target;
    }

    @Nonnull
    public EMissionType geteMissionType() {
        return eMissionType;
    }

    @Nonnull
    public EMissionAction getEMissionAction() {
        return missionAction;
    }

    public Boolean isUserDefeated() {
        return userDefeated;
    }

    public void setBattleReport(@Nonnull final BattleReport battleReport) {
        this.battleReport = Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
    }

    @Nullable
    public BattleReport getBattleReport() {
        return battleReport;
    }
}
