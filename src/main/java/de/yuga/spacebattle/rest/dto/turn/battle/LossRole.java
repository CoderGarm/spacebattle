package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

/**
 * Every loss role remembers to a defeated and destroyed war ship.
 */
public class LossRole {

    @Nonnull
    @ApiModelProperty(required = true, value = "The user which is affected by the loss.")
    private final UserJson owner;

    /**
     * Just the name of the lost ship.<br>
     * The war ship itself will be deleted.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The name of the war ship which was destroyed.")
    private final String warShipName;

    /**
     * The type of the loss.<br>
     * The ship class itself will never be removed but flagged as deleted.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The class of the war ship which was destroyed.")
    private final ShipClass shipClass;

    public LossRole(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.LossRole lossRole) {
        Preconditions.checkNotNull(lossRole, "lossRole shouldn't be null!");

        this.owner = new UserJson(lossRole.getShipClass().getOwner());
        this.warShipName = lossRole.getWarShipName();
        this.shipClass = new ShipClass(lossRole.getShipClass());
    }

    @Nonnull
    public UserJson getOwner() {
        return owner;
    }

    @Nonnull
    public String getWarShipName() {
        return warShipName;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }
}
