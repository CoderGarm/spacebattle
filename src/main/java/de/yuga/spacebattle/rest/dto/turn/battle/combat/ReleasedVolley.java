package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Schema(description = ".")
public class ReleasedVolley {

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
    @Schema(required = true, description = "The fleet which acts.")
    private AbstractId actorOwner;

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
    @Schema(required = true, description = "The type of the damage dealer.")
    private EWeaponType weaponType;

    @JsonProperty
    @Schema(required = true, description = "The amount of missiles in this salvo.")
    private int amountOfShots;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The distance of this shot.")
    private Distance initialDistance;

    public ReleasedVolley() {
    }

    public ReleasedVolley(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.ReleasedVolley input,
                          @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new AbstractId(input.getActor());
        this.actorOwner = new AbstractId(input.getActor().getOwner());
        this.target = new AbstractId(input.getTarget());
        this.damageDealer = input.getDamageDealer();
        this.weaponType = input.getWeaponType();
        this.amountOfShots = input.getAmountOfShots();
        this.initialDistance = input.getInitialDistance();
    }
}
