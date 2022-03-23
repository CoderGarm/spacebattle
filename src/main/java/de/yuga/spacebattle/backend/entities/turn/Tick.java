package de.yuga.spacebattle.backend.entities.turn;


import de.yuga.spacebattle.backend.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@NamedQueries({
        @NamedQuery(name = "Tick.getAll", query = "SELECT p FROM Tick p"),
        @NamedQuery(name = "Tick.getLatest", query = "SELECT p FROM Tick p ORDER BY p.id DESC")
})
@Entity
@Table(name = "tick")
@AttributeOverride(name = "id", column = @Column(name = "idTick"))
public class Tick extends AbstractEntityKey {

    /**
     * The tick duration in seconds.
     */
    public static final int TICK_DURATION_IN_SECONDS = 604800;

    @Nonnull
    private final static DateTimeFormatter tickFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy MM dd").toFormatter();

    @Nonnull
    @NotNull
    private final LocalDateTime tickStarts = LocalDateTime.now();

    /**
     * The timestamp of the end of the calculation.
     */
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

    @Override
    public String toString() {
        return "Tick #" + id;
    }

    @Nullable
    public LocalDateTime getTickEnds() {
        return tickEnds;
    }

    public void setTickEnds(@Nullable LocalDateTime tickEnds) {
        this.tickEnds = tickEnds;
    }

    public String convertTickToText() {
        return "Tick " + this.getId() + " at " + this.getTickStarts().format(tickFormatter);
    }
}
