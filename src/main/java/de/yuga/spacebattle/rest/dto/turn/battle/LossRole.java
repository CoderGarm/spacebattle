package de.yuga.spacebattle.rest.dto.turn.battle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * Every loss role remembers to a defeated and destroyed war ship.
 */
@Schema(description = ".")
public class LossRole {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user which is affected by the loss.")
    private UserJson owner;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet which was the war ships home.")
    private Fleet fleet;

    /**
     * Just the name of the lost ship.<br>
     * The war ship itself will be deleted.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of the war ship which was destroyed.")
    private String warShipName;

    @JsonProperty
    @Schema(required = true, description = "The id of the war ship which was destroyed.")
    private AbstractId warship;

    /**
     * The type of the loss.<br>
     * The ship class itself will never be removed but flagged as deleted.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The class of the war ship which was destroyed.")
    private AbstractId shipClass;

    public LossRole() {
    }

    public LossRole(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.LossRole lossRole,
                    @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(lossRole, "lossRole shouldn't be null!");

        this.owner = new UserJson(lossRole.getShipClass().getOwner());
        this.fleet = new Fleet(lossRole.getFleet(), languageCode);
        this.warShipName = lossRole.getWarShipName();
        this.warship = new AbstractId(lossRole.getIdWarship());
        this.shipClass = new AbstractId(lossRole.getShipClass());
    }
}
