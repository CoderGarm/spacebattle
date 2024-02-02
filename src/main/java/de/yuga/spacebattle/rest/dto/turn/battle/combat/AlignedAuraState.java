package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class AlignedAuraState {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private EWeaponAlignment alignment;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private Distance antiShipMissileRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private Distance antiMissileMissileRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private Distance weaponRange;

    public AlignedAuraState(@Nonnull final de.yuga.spacebattle.backend.combat.dto.AlignedAuraState auraState) {
        Preconditions.checkNotNull(auraState, "auraState must not be empty");

        this.alignment = auraState.getAlignment();
        this.antiShipMissileRange = auraState.getAntiShipMissileRange();
        this.antiMissileMissileRange = auraState.getAntiMissileMissileRange();
        this.weaponRange = auraState.getWeaponRange();
    }
}
