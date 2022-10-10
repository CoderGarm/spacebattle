package de.yuga.spacebattle.rest.dto.turn;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Schema(description = ".")
public class Tick {

    @Schema(required = true, description = "The number of this tick.")
    private int tickNo;

    @Nonnull
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(required = true, description = "The start timestamp of this tick.")
    private LocalDateTime tickStarts;

    @Nullable
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "The end timestamp of this tick.")
    private LocalDateTime tickEnds;

    public Tick() {
    }

    public Tick(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick tick) {
        Preconditions.checkNotNull(tick, "tick shouldn't be null!");

        this.tickNo = tick.getId();
        this.tickStarts = tick.getTickStarts();
        this.tickEnds = tick.getTickEnds();
    }

    public int getTickNo() {
        return tickNo;
    }

    @Nonnull
    public LocalDateTime getTickStarts() {
        return tickStarts;
    }

    @Nullable
    public LocalDateTime getTickEnds() {
        return tickEnds;
    }

}
