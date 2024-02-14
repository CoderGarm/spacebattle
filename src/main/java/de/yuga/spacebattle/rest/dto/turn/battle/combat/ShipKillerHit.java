package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.turn.battle.LossRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ShipKillerHit {

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
    @Schema(required = true, description = "The UUID of the damage dealer.")
    private UUID damageDealer;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The result of this salvo.")
    private EDamageResult result;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "A hit log list.")
    private final List<HitLog> hitLogs = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If the hit results in a destroyed ship, this will be logged here. By the id of the hit log.")
    private final Map<Integer, LossRole> lossesByHit = new HashMap<>();

    public ShipKillerHit() {
    }

    public ShipKillerHit(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.ShipKillerHit input,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new AbstractId(input.getActor());
        this.target = new AbstractId(input.getTarget());
        this.damageDealer = input.getDamageDealer();
        this.result = input.getResult();
        this.hitLogs.addAll(input.getHitLogs().stream().map(h -> new HitLog(h, languageCode)).collect(Collectors.toList()));
        this.lossesByHit.putAll(input.getLossesByHit().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getId(), e -> new LossRole(e.getValue(), languageCode))));
    }
}
