package de.yuga.spacebattle.backend.entities.turn.resources.trade;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.misc.Completable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tradedResource")
@AttributeOverride(name = "id", column = @Column(name = "idTradedResource"))
public class TradedResource extends Completable {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickInitiated", referencedColumnName = "idTick")
    private Tick tick;

    @Nonnull
    @OneToOne
    @JoinColumn(name = "idTradeOffer")
    private TradeOffer tradeOffer;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idBuyer")
    private Owner buyer;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idDestination")
    private Planet destination;

    public TradedResource() {
    }

    public TradedResource(@Nonnull final Tick today,
                          final int ticksLeft,
                          @Nonnull final TradeOffer tradeOffer,
                          @Nonnull final Owner buyer,
                          @Nonnull final Planet destination) {
        this.tick = Preconditions.checkNotNull(today, "today must not be empty");
        this.ticksLeft = ticksLeft;
        this.tradeOffer = Preconditions.checkNotNull(tradeOffer, "tradeOffer must not be empty");
        this.buyer = Preconditions.checkNotNull(buyer, "buyer must not be empty");
        this.destination = Preconditions.checkNotNull(destination, "destination must not be empty");
    }

    @Nonnull
    public Tick getTick() {
        return tick;
    }

    @Nonnull
    public TradeOffer getTradeOffer() {
        return tradeOffer;
    }

    @Nonnull
    public Owner getBuyer() {
        return buyer;
    }

    @Nonnull
    public Planet getDestination() {
        return destination;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final TradedResource that = (TradedResource) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
