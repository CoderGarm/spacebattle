package de.yuga.spacebattle.backend.entities.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "sharedBattleReport")
@AttributeOverride(name = "id", column = @Column(name = "idSharedBattleReport"))
public class SharedBattleReport extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idBattleReport", updatable = false, nullable = false)
    private BattleReport battleReport;

    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "participatingUsers",
            joinColumns = @JoinColumn(name = "idSharedBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idUser"),
            uniqueConstraints = @UniqueConstraint(name = "participatingUsers_UC", columnNames = {"idUser", "idSharedBattleReport"}))
    private final Set<Owner> participatingUsers = new HashSet<>();

    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sharedWithAlliances",
            joinColumns = @JoinColumn(name = "idSharedBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idAlliance"),
            uniqueConstraints = @UniqueConstraint(name = "participatingUsers_UC", columnNames = {"idAlliance", "idSharedBattleReport"}))
    private final Set<Alliance> sharedWithAlliances = new HashSet<>();

    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sharedWithUsers",
            joinColumns = @JoinColumn(name = "idSharedBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idUser"),
            uniqueConstraints = @UniqueConstraint(name = "sharedWithUsers_UC", columnNames = {"idUser", "idSharedBattleReport"}))
    private final Set<Owner> sharedWithUsers = new HashSet<>();

    @Column(columnDefinition = "boolean not null default false")
    private boolean shareWithEveryone = false;

    public SharedBattleReport() {
    }

    public SharedBattleReport(@Nonnull final BattleReport battleReport) {
        this.battleReport = Preconditions.checkNotNull(battleReport, "battleReport must not be empty");

        this.participatingUsers.addAll(battleReport.getParticipatingFleets().stream()
                .map(FleetSnapshot::getFleet)
                .map(Fleet::getOwner)
                .collect(Collectors.toSet()));
    }

    @Nonnull
    public BattleReport getBattleReport() {
        return battleReport;
    }

    @Nonnull
    public Set<Owner> getParticipatingUsers() {
        return Collections.unmodifiableSet(participatingUsers);
    }

    @Nonnull
    public Set<Alliance> getSharedWithAlliances() {
        return sharedWithAlliances;
    }

    @Nonnull
    public Set<Owner> getSharedWithUsers() {
        return sharedWithUsers;
    }

    public boolean isShareWithEveryone() {
        return shareWithEveryone;
    }

    public void setShareWithEveryone(final boolean shareWithEveryone) {
        this.shareWithEveryone = shareWithEveryone;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final SharedBattleReport that = (SharedBattleReport) o;

        return new EqualsBuilder().append(battleReport, that.battleReport).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(battleReport).toHashCode();
    }
}
