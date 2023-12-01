package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class WeaponDescriptor {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effective range of this weapon.")
    private Distance effectiveRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    @JsonProperty
    @Schema(required = true, description = "The amount of damage emitters.")
    private int damageEmitter;

    @JsonProperty
    @Schema(required = true, description = "The amount of damage.")
    private int damageValue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The needed time to reload.")
    private Time reloadTime = CombatRound.COMBAT_ROUND;

    public WeaponDescriptor() {
    }

    public WeaponDescriptor(@Nonnull final Weapon content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        this.effectiveRange = content.getDamageProjectionRange();
        this.weaponType = content.getWeaponType();
        this.damageEmitter = content.getAmountDamageEmitter();
        this.damageValue = content.getEffectValue();
    }
}
