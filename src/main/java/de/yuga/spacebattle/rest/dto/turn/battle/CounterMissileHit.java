package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class CounterMissileHit {

    @Nullable
    @Schema(required = true, description = "The round and phase information.")
    private de.yuga.spacebattle.rest.dto.turn.battle.CombatRoundKey combatRoundKey;

    @Nullable
    @Schema(required = true, description = "The fleet which acts.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet actor;

    @Nullable
    @Schema(required = true, description = "The fleet which is targeted.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet target;

    @Schema(required = true, description = "The leftover amount of missiles of the given type in the salvo.")
    private int remainingMissiles;

    @Nullable
    @Schema(required = true, description = "The UUID of the attacked missile salvo.")
    private UUID attackedMissileSalvo;

    @Schema(required = true, description = "The amount of destroyed missiles.")
    private int destroyedMissiles;

    @Nullable
    @Schema(required = true, description = "The attacked missile type as part of the salvo.")
    private de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile missile;

    public CounterMissileHit() {
    }

    public CounterMissileHit(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.CounterMissileHit input,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor(), languageCode);
        this.target = new Fleet(input.getTarget(), languageCode);
        this.remainingMissiles = input.getRemainingMissiles();
        this.attackedMissileSalvo = input.getAttackedMissileSalvo();
        this.destroyedMissiles = input.getDestroyedMissiles();
        this.missile = new Missile(input.getMissile(), languageCode);
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

    public int getRemainingMissiles() {
        return remainingMissiles;
    }

    public void setRemainingMissiles(final int remainingMissiles) {
        this.remainingMissiles = remainingMissiles;
    }

    @Nullable
    public UUID getAttackedMissileSalvo() {
        return attackedMissileSalvo;
    }

    public void setAttackedMissileSalvo(@Nullable final UUID attackedMissileSalvo) {
        this.attackedMissileSalvo = attackedMissileSalvo;
    }

    public int getDestroyedMissiles() {
        return destroyedMissiles;
    }

    public void setDestroyedMissiles(final int destroyedMissiles) {
        this.destroyedMissiles = destroyedMissiles;
    }

    @Nullable
    public Missile getMissile() {
        return missile;
    }

    public void setMissile(@Nullable final Missile missile) {
        this.missile = missile;
    }
}
