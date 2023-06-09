package de.yuga.spacebattle.backend.dto.turn.resources.trade;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;

import javax.annotation.Nonnull;
import java.util.List;

public class TradesInTimeframe {

    @Nonnull
    private final List<Tick> timeframe;

    @Nonnull
    private final List<TradedResource> trades;

    public TradesInTimeframe(@Nonnull final List<Tick> timeframe, @Nonnull final List<TradedResource> trades) {
        this.timeframe = Preconditions.checkNotNull(timeframe, "timeframe must not be empty");
        this.trades = Preconditions.checkNotNull(trades, "trades must not be empty");
    }

    @Nonnull
    public List<TradedResource> getTrades() {
        return trades;
    }

    @Nonnull
    public List<Tick> getTimeframe() {
        return timeframe;
    }
}
