package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class MissileMovement {

    @Nullable
    @Schema(required = true, description = "The round and phase information.")
    private de.yuga.spacebattle.rest.dto.turn.battle.CombatRoundKey combatRoundKey;

    @Nullable
    @Schema(required = true, description = "The fleet which acts.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet actor;

    @Nullable
    @Schema(required = true, description = "The fleet which is targeted.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet target;

    @Nullable
    @Schema(required = true, description = "The UUID of the moving missile salvo.")
    private UUID movingMissileSalvo;

    @Schema(required = true, description = "The amount of missiles in this salvo.")
    private int missileAmount;

    @Schema(required = true, description = "The amount of rounds which have to be passed before in range for a hit.")
    private int roundsToTravel;

    @Nullable
    @Schema(required = true, description = "The current position of the salvo.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit position;

    @Nullable
    @Schema(required = true, description = "The position of the salvo in the last round.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit lastPosition;

    @Nullable
    @Schema(required = true, description = "The current position of the target.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit targetPosition;

    public MissileMovement() {
    }

    public MissileMovement(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MissileMovement input,
                           @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor(), languageCode);
        this.target = new Fleet(input.getTarget(), languageCode);
        this.movingMissileSalvo = input.getMovingMissileSalvo();
        this.missileAmount = input.getMissileAmount();
        this.roundsToTravel = input.getRoundsToTravel();
        this.position = new Orbit(input.getPosition());
        this.lastPosition = new Orbit(input.getLastPosition());
        this.targetPosition = new Orbit(input.getTargetPosition());
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
    public UUID getMovingMissileSalvo() {
        return movingMissileSalvo;
    }

    public void setMovingMissileSalvo(@Nullable final UUID movingMissileSalvo) {
        this.movingMissileSalvo = movingMissileSalvo;
    }

    public int getMissileAmount() {
        return missileAmount;
    }

    public void setMissileAmount(final int missileAmount) {
        this.missileAmount = missileAmount;
    }

    public int getRoundsToTravel() {
        return roundsToTravel;
    }

    public void setRoundsToTravel(final int roundsToTravel) {
        this.roundsToTravel = roundsToTravel;
    }

    @Nullable
    public Orbit getPosition() {
        return position;
    }

    public void setPosition(@Nullable final Orbit position) {
        this.position = position;
    }

    @Nullable
    public Orbit getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(@Nullable final Orbit lastPosition) {
        this.lastPosition = lastPosition;
    }

    @Nullable
    public Orbit getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(@Nullable final Orbit targetPosition) {
        this.targetPosition = targetPosition;
    }
}
