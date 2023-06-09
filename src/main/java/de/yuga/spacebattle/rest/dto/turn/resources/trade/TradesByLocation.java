package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Schema(description = ".")
public class TradesByLocation {

    @Nullable
    @JsonProperty
    @Schema(description = "The seller's id.")
    private AbstractId seller;

    @Nullable
    @JsonProperty
    @Schema(description = "The origin planet of the goods.")
    private AbstractId origin;

    @Nullable
    @JsonProperty
    @Schema(description = "The buyer's id.")
    private AbstractId buyer;

    @Nullable
    @JsonProperty
    @Schema(description = "The destination planet of the good.")
    private AbstractId destination;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The tickstamped trades.")
    private Set<TradesByTick> tradesByTick = new HashSet<>();

    public TradesByLocation() {
    }

    public TradesByLocation(@Nullable final Planet origin, @Nullable final Planet destination) {
        if (origin != null) {
            this.origin = new AbstractId(origin, origin.getName());
            final Owner owner = origin.getOwner();
            this.seller = new AbstractId(Objects.requireNonNull(owner), owner.getUsername());
        }
        if (destination != null) {
            this.destination = new AbstractId(destination, destination.getName());
            final Owner owner = destination.getOwner();
            this.buyer = new AbstractId(Objects.requireNonNull(owner), owner.getUsername());
        }
    }

    @JsonIgnore
    public void add(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick tick,
                    final int ticksLeft,
                    @Nonnull final EResourceType eResourceType,
                    final long price,
                    final long amount) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(eResourceType, "eResourceType must not be empty");

        final int tickNo = tick.getNo() + ticksLeft;
        final LocalDateTime tickStarts = tick.getTickStarts().plusDays(ticksLeft);


        final Tick deliveryAt = new Tick(tickNo, tickStarts);
        final Trade trade = new Trade(price, eResourceType, amount);
        TradesByTick byTick = tradesByTick.stream()
                .filter(t -> t.matchesTick(deliveryAt))
                .findFirst()
                .orElse(null);
        if (byTick == null) {
            byTick = new TradesByTick(deliveryAt);
            this.tradesByTick.add(byTick);
        }
        byTick.add(trade);
    }
}
