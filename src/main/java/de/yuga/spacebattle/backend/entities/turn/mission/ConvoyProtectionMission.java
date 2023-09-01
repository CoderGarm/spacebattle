package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.MissionType;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue(MissionType.CONVOY_PROTECTION)
public class ConvoyProtectionMission extends Mission {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTradeResource")
    private TradedResource protectedTrade;

    public ConvoyProtectionMission() {
    }

    public ConvoyProtectionMission(@Nonnull final User actor, @Nonnull final Tick today, @Nonnull final TradedResource protectedTrade) {
        super(actor, today);

        this.protectedTrade = Preconditions.checkNotNull(protectedTrade, "protectedTrade must not be empty");
    }

    @Nonnull
    public TradedResource getProtectedTrade() {
        return protectedTrade;
    }
}
