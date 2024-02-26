package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EHitArea;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class HitLog {

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private CombatRoundKey combatRoundKey;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The UUID of the damage dealer.")
    private UUID damageDealer;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The attacked warship.")
    private AbstractId warShip;

    @JsonProperty
    @Schema(required = true, description = "The applied damage.")
    private long damageValue;

    @JsonProperty
    @Schema(required = true, description = "The remaining hit points of the attacked part of the ship.")
    private int state;

    /**
     * The attacked part of the ship.
     */
    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The attacked part of the ship.")
    private EHitArea attackedPart;

    @JsonProperty
    @Schema(required = true, description = "If the ship is alive after damage.")
    private boolean isAlive;

    @JsonProperty
    @Schema(required = true, description = "If the ship is capable of staying in the battle after damage.")
    private boolean isFightingCapable;

    public HitLog() {
    }

    public HitLog(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.HitLog input,
                  @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound());
        this.damageDealer = input.getDamageDealer();
        this.warShip = new AbstractId(input.getWarShip());
        this.damageValue = input.getDamageValue();
        this.state = input.getState();
        this.attackedPart = input.getAttackedPart();
        this.isAlive = input.isAlive();
        this.isFightingCapable = input.isFightingCapable();
    }
}
