package de.yuga.spacebattle.backend.entities.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.BattleResult;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "BattleReport.findAllWithUser", query = "SELECT r FROM BattleReport r LEFT JOIN r.participatingUsers u ON (u.id = :idUser)"),
        @NamedQuery(name = "BattleReport.findLatestWithUser", query = "SELECT r FROM BattleReport r LEFT JOIN r.participatingUsers u ON (u.id = :idUser) ORDER BY r.tick.id DESC"),
})
@Entity
@Table(name = "battleReport")
@AttributeOverride(name = "id", column = @Column(name = "idBattleReport"))
public class BattleReport extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTick")
    private Tick tick;

    /**
     * The place to be.
     */
    @Nonnull
    @NotNull
    @Embedded
    private FleetOrbit orbit;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "participatingUsers",
            joinColumns = @JoinColumn(name = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idUser"),
            uniqueConstraints = @UniqueConstraint(name = "participatingUsers_UC", columnNames = {"idUser", "idBattleReport"}))
    private final Set<User> participatingUsers = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lossRole", joinColumns = @JoinColumn(name = "idBattleReport"))
    private final List<LossRole> lossRole = new ArrayList<>();

    public BattleReport() {
    }

    public BattleReport(@Nonnull final Tick tick, @Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(tick, "tick shouldn't be null!");
        Preconditions.checkNotNull(battleResult, "fightingResult shouldn't be null!");

        this.tick = tick;
        this.orbit = battleResult.getFleetClash().getOrbit();
        this.lossRole.addAll(battleResult.getLosses().stream().map(LossRole::new).collect(Collectors.toList()));
        this.participatingUsers.addAll(battleResult.getFleetClash().getParticipatingFleets().stream().map(Fleet::getOwner).collect(Collectors.toSet()));
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
    public Set<User> getParticipatingUsers() {
        return participatingUsers;
    }

    @Nonnull
    public List<LossRole> getLossRole() {
        return lossRole;
    }
}
