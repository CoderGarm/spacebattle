package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class MissileMovement {

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private CombatRoundKey combatRoundKey;

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

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The UUID of the moving missile salvo.")
    private UUID movingMissileSalvo;

    @JsonProperty
    @Schema(required = true, description = "The amount of missiles in this salvo.")
    private int missileAmount;

    @JsonProperty
    @Schema(required = true, description = "The amount of rounds which have to be passed before in range for a hit.")
    private int roundsToTravel;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The current position of the salvo.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit position;

    public MissileMovement() {
    }

    public MissileMovement(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MissileMovement input,
                           @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new AbstractId(input.getActor());
        this.actorOwner = new AbstractId(input.getActor().getOwner());
        this.target = new AbstractId(input.getTarget());
        this.movingMissileSalvo = input.getMovingMissileSalvo();
        this.missileAmount = input.getMissileAmount();
        this.roundsToTravel = input.getRoundsToTravel();
        this.position = new Orbit(input.getPosition());
    }
}
