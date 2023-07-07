package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mission")
@AttributeOverride(name = "id", column = @Column(name = "idMission"))
@DiscriminatorColumn(name = "missionType", discriminatorType = DiscriminatorType.STRING)
public class Mission extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idActor", updatable = false)
    private User actor;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickStartedAt")
    private Tick started;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickStoppedAt")
    private Tick stopped;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EMissionType missionType;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idMission")
    private final Set<WarShip> ships = new HashSet<>();

    /**
     * The place to be.
     */
    @Nonnull
    @NotNull
    @Embedded
    private FleetOrbit venue;
}
