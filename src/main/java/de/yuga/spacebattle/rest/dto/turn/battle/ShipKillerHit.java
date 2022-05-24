package de.yuga.spacebattle.rest.dto.turn.battle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ShipKillerHit {

    @Nullable
    @Schema(required = true, description = "The round and phase information.")
    private CombatRoundKey combatRoundKey;

    @Nullable
    @Schema(required = true, description = "The fleet which acts.")
    private Fleet actor;

    @Nullable
    @Schema(required = true, description = "The fleet which is targeted.")
    private Fleet target;

    @Nullable
    @Schema(required = true, description = "The UUID of the damage dealer.")
    private UUID damageDealer;

    @Nullable
    @Schema(required = true, description = "The distance of this shot.")
    private Distance distance;

    @Nullable
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

    public ShipKillerHit(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.ShipKillerHit input) {
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor());
        this.target = new Fleet(input.getTarget());
        this.damageDealer = input.getDamageDealer();
        this.distance = input.getDistance();
        this.result = input.getResult();
        this.hitLogs.addAll(input.getHitLogs().stream().map(HitLog::new).collect(Collectors.toList()));
        this.lossesByHit.putAll(input.getLossesByHit().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getId(), e -> new LossRole(e.getValue()))));
    }

    @Nullable
    public CombatRoundKey getCombatRoundKey() {
        return combatRoundKey;
    }

    public void setCombatRoundKey(@Nullable final CombatRoundKey combatRoundKey) {
        this.combatRoundKey = combatRoundKey;
    }

    @Nullable
    public Fleet getActor() {
        return actor;
    }

    public void setActor(@Nullable final Fleet actor) {
        this.actor = actor;
    }

    @Nullable
    public Fleet getTarget() {
        return target;
    }

    public void setTarget(@Nullable final Fleet target) {
        this.target = target;
    }

    @Nullable
    public UUID getDamageDealer() {
        return damageDealer;
    }

    public void setDamageDealer(@Nullable final UUID damageDealer) {
        this.damageDealer = damageDealer;
    }

    @Nullable
    public Distance getDistance() {
        return distance;
    }

    public void setDistance(@Nullable final Distance distance) {
        this.distance = distance;
    }

    @Nullable
    public EDamageResult getResult() {
        return result;
    }

    public void setResult(@Nullable final EDamageResult result) {
        this.result = result;
    }
}
