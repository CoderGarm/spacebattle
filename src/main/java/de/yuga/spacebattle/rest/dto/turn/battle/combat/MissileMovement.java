package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class MissileMovement {

    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private int combatRoundKey;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private AbstractId actor;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private AbstractId actorOwner;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The fleet which is targeted.")
    private AbstractId target;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The UUID of the moving missile salvo.")
    private UUID movingMissileSalvo;

    @JsonProperty
    @Schema(required = true, description = "The amount of missiles in this salvo.")
    private int missileAmount;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The total length on the main track.")
    private Distance lengthOnTrack;

    public MissileMovement() {
    }

    public MissileMovement(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MissileMovement input,
                           @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = input.getCombatRound().getNo();
        final Maneuver maneuver = input.getManeuver();
        this.actor = new AbstractId(input.getActor());
        this.actorOwner = new AbstractId(input.getActor().getOwner());
        this.target = new AbstractId(input.getTarget());
        this.movingMissileSalvo = input.getMovingMissileSalvo();
        this.missileAmount = input.getMissileAmount();
        this.lengthOnTrack = input.getLengthOnTrack();
    }
}
