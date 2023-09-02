package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "missionItem")
@AttributeOverride(name = "id", column = @Column(name = "idMissionItem"))
@DiscriminatorColumn(name = "missionType", discriminatorType = DiscriminatorType.STRING)
public class MissionItem extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickCreatedAt")
    private Tick createdAt;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private EMissionType missionType;

    public MissionItem() {
    }

    public MissionItem(@Nonnull final Tick createdAt) {
        this.createdAt = Preconditions.checkNotNull(createdAt, "tick must not be empty");
    }

    @Nonnull
    public Tick getCreatedAt() {
        return createdAt;
    }

    public void setMissionType(@Nonnull final EMissionType missionType) {
        this.missionType = Preconditions.checkNotNull(missionType, "missionType must not be empty");
    }

    /**
     * States the raiding result onto a convoy.
     */
    @Nonnull
    public static ConvoyProtectionMissionItem convoyRaided(@Nonnull final Tick tick,
                                                           @Nonnull final TradedResource tradedResource,
                                                           @Nonnull final EMissionAction action,
                                                           final int percentOfCargoLost,
                                                           final boolean isRansomPayment) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");
        Preconditions.checkNotNull(action, "action must not be empty");
        Preconditions.checkState(action == EMissionAction.BEGIN_OF_MISSION || action == EMissionAction.END_OF_MISSION, "action must not be chosen wisely");

        return new ConvoyProtectionMissionItem(tick, tradedResource, percentOfCargoLost, isRansomPayment, action);
    }

    /**
     * States that the escort ships are scary enough.
     */
    @Nonnull
    public static ConvoyProtectionMissionItem convoyGuardedOnSight(@Nonnull final Tick tick, @Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        final ConvoyProtectionMissionItem whatever = new ConvoyProtectionMissionItem(tick, tradedResource, EMissionAction.END_OF_MISSION);
        whatever.setPiratedWithdraw();
        return whatever;
    }

    /**
     * States that the pirates take a try but accepted their failure.
     */
    @Nonnull
    public static ConvoyProtectionMissionItem convoyGuardedWithShipContact(@Nonnull final Tick tick, @Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(tick, "tick must not be empty");
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        final ConvoyProtectionMissionItem whatever = new ConvoyProtectionMissionItem(tick, tradedResource, EMissionAction.END_OF_MISSION);
        whatever.setPiratedWithdrawAfterApproach();
        return whatever;
    }

}
