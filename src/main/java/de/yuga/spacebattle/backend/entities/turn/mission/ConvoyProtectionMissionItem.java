package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.MissionType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue(MissionType.CONVOY_PROTECTION)
public class ConvoyProtectionMissionItem extends MissionItem {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTradeResource")
    private TradedResource affectedTrade;

    private int percentOfCargoLost = 0;

    private boolean isRansomPayment = false;

    private boolean piratedWithdraw = false;

    private boolean piratedWithdrawAfterApproach = false;

    /**
     * EMissionAction.BEGIN_OF_MISSION for raid at beginning, EMissionAction.END_OF_MISSION for raid at the end.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EMissionAction phase;

    public ConvoyProtectionMissionItem() {
    }

    public ConvoyProtectionMissionItem(@Nonnull final Tick createdAt,
                                       final TradedResource affectedTrade,
                                       final int percentOfCargoLost,
                                       final boolean isRansomPayment,
                                       @Nonnull final EMissionAction phase) {
        this(createdAt, affectedTrade, phase);

        this.percentOfCargoLost = percentOfCargoLost;
        this.isRansomPayment = isRansomPayment;
    }

    public ConvoyProtectionMissionItem(@Nonnull final Tick createdAt,
                                       @Nonnull final TradedResource affectedTrade,
                                       @Nonnull final EMissionAction phase) {
        super(createdAt);
        Preconditions.checkNotNull(affectedTrade, "affectedTrade must not be empty");
        Preconditions.checkNotNull(phase, "action must not be empty");
        Preconditions.checkState(phase == EMissionAction.BEGIN_OF_MISSION || phase == EMissionAction.END_OF_MISSION, "action must not be chosen wisely");

        this.affectedTrade = affectedTrade;
        this.phase = phase;
    }

    @Nonnull
    public TradedResource getAffectedTrade() {
        return affectedTrade;
    }

    public int getPercentOfCargoLost() {
        return percentOfCargoLost;
    }

    public boolean isRansomPayment() {
        return isRansomPayment;
    }

    public boolean isPiratedWithdraw() {
        return piratedWithdraw;
    }

    public boolean isPiratedWithdrawAfterApproach() {
        return piratedWithdrawAfterApproach;
    }

    @Nonnull
    public EMissionAction getPhase() {
        return phase;
    }

    public void setPiratedWithdraw() {
        this.piratedWithdraw = true;
    }


    public void setPiratedWithdrawAfterApproach() {
        this.piratedWithdrawAfterApproach = true;
    }
}
