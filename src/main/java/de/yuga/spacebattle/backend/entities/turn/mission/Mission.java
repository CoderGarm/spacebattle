package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "mission")
@AttributeOverride(name = "id", column = @Column(name = "idMission"))
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


}
