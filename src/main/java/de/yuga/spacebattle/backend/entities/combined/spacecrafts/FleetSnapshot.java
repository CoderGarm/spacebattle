package de.yuga.spacebattle.backend.entities.combined.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "fleetSnapshot")
@AttributeOverride(name = "id", column = @Column(name = "idFleetSnapshot"))
public class FleetSnapshot extends Deletable implements HasOwner {

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idBattleReport")
    private BattleReport battleReport;

    /**
     * The fleet which is this snap for.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idFleet")
    private Fleet fleet;

    @Nonnull
    @NotNull
    private String name;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "idFleetSnapshot")
    private final Set<WarshipHealthStateSnapshot> ships = new HashSet<>();

    public FleetSnapshot() {
    }

    public FleetSnapshot(@Nonnull final BattleReport battleReport,
                         @Nonnull final Fleet fleet) {
        this(fleet, fleet.getAliveShips());
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");

        this.battleReport = battleReport;
    }

    public FleetSnapshot(@Nonnull final Fleet fleet, @Nonnull final Set<WarShip> ships) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(ships, "ships must not be empty");

        this.fleet = fleet;
        this.owner = fleet.getOwner();
        this.name = fleet.getName();
        this.ships.addAll(ships.stream().map(w -> new WarshipHealthStateSnapshot(this, w)).collect(Collectors.toSet()));
    }

    @Nullable
    public BattleReport getBattleReport() {
        return battleReport;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public String getName() {
        return name;
    }


    public void setOwner(@Nonnull final Owner owner) {
        this.owner = Preconditions.checkNotNull(owner, "owner must not be empty");
    }

    @Nonnull
    @Override
    public Owner getOwner() {
        return owner;
    }

    @Nullable
    @Override
    public User getHumanOwner() {
        if (!(owner instanceof User)) {
            return null;
        }
        return (User) owner;
    }

    @Nullable
    @Override
    public NonPlayerCharacter getNpcOwner() {
        if (!(owner instanceof NonPlayerCharacter)) {
            return null;
        }
        return (NonPlayerCharacter) owner;
    }

    @Nonnull
    public Set<WarshipHealthStateSnapshot> getShips() {
        return ships;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FleetSnapshot that = (FleetSnapshot) o;

        return new EqualsBuilder().append(battleReport, that.battleReport).append(fleet, that.fleet).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(battleReport).append(fleet).toHashCode();
    }
}
