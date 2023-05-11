package de.yuga.spacebattle.rest.dto.turn.resources.trade;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class TradesByTick {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The scope of this set of prices.")
    private Tick tick;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effective prices of the resources.")
    private List<Trade> trades = new ArrayList<>();

    public TradesByTick() {
    }

    public TradesByTick(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick tick, @Nonnull final List<Trade> trades) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(trades, "trades must not be empty");

        this.tick = new Tick(tick);
        this.trades = trades;
    }

    public TradesByTick(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        this.tick = new Tick(tick);
    }

    @JsonIgnore
    public boolean matchesTick(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return this.tick.getTickNo() == tick.getTickNo();
    }

    public void add(@Nonnull final Trade trade) {
        Preconditions.checkNotNull(trade, "trade must not be empty");

        this.trades.add(trade);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final TradesByTick byTick = (TradesByTick) o;

        return new EqualsBuilder().append(tick.getTickNo(), byTick.tick.getTickNo()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(tick.getTickNo()).toHashCode();
    }
}
