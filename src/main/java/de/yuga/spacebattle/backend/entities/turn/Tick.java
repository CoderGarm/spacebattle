package de.yuga.spacebattle.backend.entities.turn;


import de.yuga.spacebattle.backend.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(name = "Tick.getAll", query = "SELECT p FROM Tick p"),
        @NamedQuery(name = "Tick.getLatest", query = "SELECT p FROM Tick p ORDER BY p.idTick DESC")
})
@Entity
@Table(name = "tick")
@AttributeOverride(name = "id", column = @Column(name = "idTick"))
public class Tick extends AbstractEntityKey {

    @Nonnull
    @NotNull
    private final LocalDateTime tickStarts = LocalDateTime.now();

    @Nullable
    private LocalDateTime tickEnds;

    public Tick() {
    }

    @Nonnull
    public LocalDateTime getTickStarts() {
        return tickStarts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tick)) return false;
        Tick tick = (Tick) o;
        return id == tick.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }

    @Nullable
    public LocalDateTime getTickEnds() {
        return tickEnds;
    }

    public void setTickEnds(@Nullable LocalDateTime tickEnds) {
        this.tickEnds = tickEnds;
    }
}
