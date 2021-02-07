package de.yuga.spacebattle.entities.turn;


import de.yuga.spacebattle.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(name = "Tick.getAll", query = "SELECT p FROM Tick p")
})
@Entity
@Table(name = "tick")
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
