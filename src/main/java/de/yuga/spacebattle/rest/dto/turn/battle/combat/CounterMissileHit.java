package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class CounterMissileHit {

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
    @Schema(required = true, description = "The fleet which is targeted.")
    private AbstractId target;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The UUID of the attacked missile salvo.")
    private UUID attackedMissileSalvo;

    @JsonProperty
    @Schema(required = true, description = "The amount of destroyed missiles.")
    private int destroyedMissiles;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The attacked missile type as part of the salvo.")
    private AbstractId missile;

    public CounterMissileHit() {
    }

    public CounterMissileHit(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.CounterMissileHit input,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new AbstractId(input.getActor());
        this.target = new AbstractId(input.getTarget());
        this.attackedMissileSalvo = input.getAttackedMissileSalvo();
        this.destroyedMissiles = input.getDestroyedMissiles();
        this.missile = new AbstractId(input.getMissile());
    }
}
