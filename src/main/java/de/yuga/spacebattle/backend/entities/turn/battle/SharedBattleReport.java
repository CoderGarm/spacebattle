package de.yuga.spacebattle.backend.entities.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sharedBattleReport")
@AttributeOverride(name = "id", column = @Column(name = "idSharedBattleReport"))
public class SharedBattleReport extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "idBattleReport")
    private BattleReport battleReport;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sharedWithUsers",
            joinColumns = @JoinColumn(name = "idSharedBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idUser"),
            uniqueConstraints = @UniqueConstraint(name = "sharedWithUsers_UC", columnNames = {"idUser", "idSharedBattleReport"}))
    private final Set<User> sharedWithUsers = new HashSet<>();

    @Column(columnDefinition = "boolean not null default false")
    private boolean shareWithEveryone = false;

    public SharedBattleReport() {
    }

    public SharedBattleReport(@Nonnull final BattleReport battleReport) {
        this.battleReport = Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
    }

    @Nonnull
    public BattleReport getBattleReport() {
        return battleReport;
    }

    @Nullable
    public Alliance getAlliance() {
        return alliance;
    }

    public void setAlliance(@Nullable final Alliance alliance) {
        this.alliance = alliance;
    }

    @Nonnull
    public Set<User> getSharedWithUsers() {
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
