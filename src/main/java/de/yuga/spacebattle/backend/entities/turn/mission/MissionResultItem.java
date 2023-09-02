package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "missionResultItem")
@AttributeOverride(name = "id", column = @Column(name = "idMissionResultItem"))
public class MissionResultItem extends AbstractEntityKey {

    @Nonnull
    @NotNull
    private String key;

    @Nonnull
    @NotNull
    private String clazzName;

    @Lob
    @Nonnull
    @NotNull
    private String effect;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickCreated")
    private Tick createdAt;

    public MissionResultItem() {
    }

}
