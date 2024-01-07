package de.yuga.spacebattle.backend.entities.events;

import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "userPoints")
@AttributeOverride(name = "id", column = @Column(name = "idUserPoints"))
public class UserPoints extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EGameEvent gameEvent;


}
