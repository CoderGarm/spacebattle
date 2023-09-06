package de.yuga.spacebattle.rest.dto.turn.resources.trade;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Schema(description = ".")
public class ValueTradesByTick {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The scope of this set of prices.")
    private Tick tick;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The trades.")
    private List<Trade> trades = new ArrayList<>();


    public ValueTradesByTick() {
    }

    public ValueTradesByTick(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick tick, @Nonnull final List<Trade> trades) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(trades, "trades must not be empty");

        this.tick = new Tick(tick);
        this.trades = trades;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ValueTradesByTick byTick = (ValueTradesByTick) o;

        return new EqualsBuilder().append(tick.getTickNo(), byTick.tick.getTickNo()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(tick.getTickNo()).toHashCode();
    }
}
