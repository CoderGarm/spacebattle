package de.yuga.spacebattle.rest.dto.turn;


import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

public class Tick {

    @ApiModelProperty(required = true, value = "The number of this tick.")
    private final int tickNo;

    @Nonnull
    @ApiModelProperty(required = true, value = "The start timestamp of this tick.")
    private final LocalDateTime tickStarts;

    @Nullable
    @ApiModelProperty(value = "The end timestamp of this tick.")
    private final LocalDateTime tickEnds;

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
