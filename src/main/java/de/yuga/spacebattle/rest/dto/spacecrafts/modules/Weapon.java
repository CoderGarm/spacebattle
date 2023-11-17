package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Weapon extends WithCosts<Weapon> {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    /**
     * Defines the range of this weapon.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effective range of this weapon.")
    private Distance effectiveRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    public Weapon() {
    }

    public Weapon(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon weapon,
                  @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(weapon, "weapon shouldn't be null!");

        this.effectiveRange = weapon.getDamageProjectionRange();
        this.weaponType = weapon.getWeaponType();
        this.baseModule = new BaseModule(weapon, languageCode);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

}
