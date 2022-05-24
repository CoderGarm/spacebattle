package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class ReleasedVolley {

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
    @Schema(required = true, description = "The type of the damage dealer.")
    private EWeaponType weaponType;

    @Schema(required = true, description = "The amount of missiles in this salvo.")
    private int amountOfShots;

    @Nullable
    @Schema(required = true, description = "The distance of this shot.")
    private Distance initialDistance;


    public ReleasedVolley(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.ReleasedVolley input) {
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor());
        this.target = new Fleet(input.getTarget());
        this.damageDealer = input.getDamageDealer();
        this.weaponType = input.getWeaponType();
        this.amountOfShots = input.getAmountOfShots();
        this.initialDistance = input.getInitialDistance();
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
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(@Nullable final EWeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public int getAmountOfShots() {
        return amountOfShots;
    }

    public void setAmountOfShots(final int amountOfShots) {
        this.amountOfShots = amountOfShots;
    }

    @Nullable
    public Distance getInitialDistance() {
        return initialDistance;
    }

    public void setInitialDistance(@Nullable final Distance initialDistance) {
        this.initialDistance = initialDistance;
    }
}
