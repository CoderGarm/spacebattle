package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EHitArea;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class HitLog {

    @Nullable
    @Schema(required = true, description = "The round and phase information.")
    private de.yuga.spacebattle.rest.dto.turn.battle.CombatRoundKey combatRoundKey;

    @Nullable
    @Schema(required = true, description = "The UUID of the damage dealer.")
    private UUID damageDealer;

    @Nullable
    @Schema(required = true, description = "The attacked warship.")
    private de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip warShip;

    @Nullable
    @Schema(required = true, description = "The string representation of the target's health state.")
    private String warshipHealthState;

    @Schema(required = true, description = "The applied damage.")
    private long damageValue;

    @Schema(required = true, description = "The remaining hit points of the attacked part of the ship.")
    private int state;

    /**
     * The attacked part of the ship.
     */
    @Nullable
    @Schema(required = true, description = "The attacked part of the ship.")
    private EHitArea attackedPart;

    @Schema(required = true, description = "If the ship is alive after damage.")
    private boolean isAlive;

    @Schema(required = true, description = "If the ship is capable of staying in the battle after damage.")
    private boolean isFightingCapable;

    public HitLog() {
    }

    public HitLog(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.HitLog input,
                  @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.damageDealer = input.getDamageDealer();
        this.warShip = new WarShip(input.getWarShip(), languageCode);
        this.warshipHealthState = input.getWarshipHealthState();
        this.damageValue = input.getDamageValue();
        this.state = input.getState();
        this.attackedPart = input.getAttackedPart();
        this.isAlive = input.isAlive();
        this.isFightingCapable = input.isFightingCapable();
    }

    @Nullable
    public CombatRoundKey getCombatRoundKey() {
        return combatRoundKey;
    }

    public void setCombatRoundKey(@Nullable final CombatRoundKey combatRoundKey) {
        this.combatRoundKey = combatRoundKey;
    }

    @Nullable
    public UUID getDamageDealer() {
        return damageDealer;
    }

    public void setDamageDealer(@Nullable final UUID damageDealer) {
        this.damageDealer = damageDealer;
    }

    @Nullable
    public WarShip getWarShip() {
        return warShip;
    }

    public void setWarShip(@Nullable final WarShip warShip) {
        this.warShip = warShip;
    }

    @Nullable
    public String getWarshipHealthState() {
        return warshipHealthState;
    }

    public void setWarshipHealthState(@Nullable final String warshipHealthState) {
        this.warshipHealthState = warshipHealthState;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public void setDamageValue(final long damageValue) {
        this.damageValue = damageValue;
    }

    public int getState() {
        return state;
    }

    public void setState(final int state) {
        this.state = state;
    }

    @Nullable
    public EHitArea getAttackedPart() {
        return attackedPart;
    }

    public void setAttackedPart(@Nullable final EHitArea attackedPart) {
        this.attackedPart = attackedPart;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(final boolean alive) {
        isAlive = alive;
    }

    public boolean isFightingCapable() {
        return isFightingCapable;
    }

    public void setFightingCapable(final boolean fightingCapable) {
        isFightingCapable = fightingCapable;
    }
}
