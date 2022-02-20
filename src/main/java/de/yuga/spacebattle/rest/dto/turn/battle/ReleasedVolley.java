package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

public class ReleasedVolley {

    @Nullable
    @ApiModelProperty(required = true, value = "The round and phase information.")
    private CombatRoundKey combatRoundKey;

    @Nullable
    @ApiModelProperty(required = true, value = "The fleet which acts.")
    private Fleet actor;

    @Nullable
    @ApiModelProperty(required = true, value = "The fleet which is targeted.")
    private Fleet target;

    @Nullable
    @ApiModelProperty(required = true, value = "The UUID of the damage dealer.")
    private UUID damageDealer;

    @Nullable
    @ApiModelProperty(required = true, value = "The type of the damage dealer.")
    private EWeaponType weaponType;

    @ApiModelProperty(required = true, value = "The amount of missiles in this salvo.")
    private int amountOfShots;

    @Nullable
    @ApiModelProperty(required = true, value = "The distance of this shot.")
    private BigDecimal initialDistance;


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
    public BigDecimal getInitialDistance() {
        return initialDistance;
    }

    public void setInitialDistance(@Nullable final BigDecimal initialDistance) {
        this.initialDistance = initialDistance;
    }
}
