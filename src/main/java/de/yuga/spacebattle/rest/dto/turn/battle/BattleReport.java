package de.yuga.spacebattle.rest.dto.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleReport {

    @ApiModelProperty(required = true, value = "The database id of the report.")
    private final int idBattleReport;

    @Nonnull
    @ApiModelProperty(required = true, value = "The tick where the action happened.")
    private final Tick tick;

    /**
     * The place to be.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The place where the action happened.")
    private final FleetOrbit orbit;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The participating users.")
    private final Set<UserJson> participatingUsers = new HashSet<>();

    @Nonnull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lossRole")
    private final List<de.yuga.spacebattle.rest.dto.turn.battle.LossRole> lossRole = new ArrayList<>();

    public BattleReport(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        this.idBattleReport = battleReport.getId();
        this.tick = new de.yuga.spacebattle.rest.dto.turn.Tick(battleReport.getTick());
        this.orbit = new FleetOrbit(battleReport.getOrbit());
        this.lossRole.addAll(battleReport.getLossRole().stream().map(de.yuga.spacebattle.rest.dto.turn.battle.LossRole::new).collect(Collectors.toList()));
        this.participatingUsers.addAll(battleReport.getParticipatingUsers().stream().map(UserJson::new).collect(Collectors.toSet()));
    }

    public int getIdBattleReport() {
        return idBattleReport;
    }

    @Nonnull
    public Tick getTick() {
        return tick;
    }

    @Nonnull
    public FleetOrbit getOrbit() {
        return orbit;
    }

    @Nonnull
    public Set<UserJson> getParticipatingUsers() {
        return participatingUsers;
    }

    @Nonnull
    public List<LossRole> getLossRole() {
        return lossRole;
    }
}
