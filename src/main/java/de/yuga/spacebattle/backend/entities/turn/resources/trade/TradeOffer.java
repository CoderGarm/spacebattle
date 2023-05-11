package de.yuga.spacebattle.backend.entities.turn.resources.trade;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tradeOffer")
@AttributeOverride(name = "id", column = @Column(name = "idTradeOffer"))
public class TradeOffer extends Deletable {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickInitiated", referencedColumnName = "idTick")
    private Tick tick;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idSeller")
    private User seller;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idOrigin")
    private Planet origin;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EResourceType resourceType;

    private long amount;

    private long price;

    public TradeOffer() {
    }

    public TradeOffer(@Nonnull final Tick today,
                      @Nonnull final User seller,
                      @Nonnull final Planet origin,
                      @Nonnull final EResourceType resourceType,
                      final long amount,
                      final long price) {
        this.tick = Preconditions.checkNotNull(today, "today must not be empty");
        this.seller = Preconditions.checkNotNull(seller, "seller must not be empty");
        this.origin = Preconditions.checkNotNull(origin, "origin must not be empty");
        this.resourceType = Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        this.amount = amount;
        this.price = price;

    }

    public void setTick(@Nonnull final Tick tick) {
        this.tick = Preconditions.checkNotNull(tick, "tick must not be empty");
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @Nonnull
    public Tick getTick() {
        return tick;
    }

    @Nonnull
    public User getSeller() {
        return seller;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    public long getAmount() {
        return amount;
    }

    public long getPrice() {
        return price;
    }

    @Nonnull
    public Planet getOrigin() {
        return origin;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final TradeOffer that = (TradeOffer) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
