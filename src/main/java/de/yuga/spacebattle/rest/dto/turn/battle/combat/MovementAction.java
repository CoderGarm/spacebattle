package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Set;

@Schema(description = ".")
public class MovementAction {

    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private int combatRoundKey;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private AbstractId actor;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The selected movement option for this action.")
    private EMovementType movementType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The aura range states.")
    private AuraState auraState;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The total length on the main track.")
    private Distance lengthOnTrack;

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction input,
                          @Nonnull final String languageCode,
                          @Nonnull final Set<FleetSnapshot> participatingFleets) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");
        Preconditions.checkNotNull(participatingFleets, "participatingFleets must not be empty");

        this.combatRoundKey = input.getCombatRound().getNo();
        final FleetSnapshot fleetSnapshot = participatingFleets.stream()
                .filter(snap -> snap.getFleet().equals(input.getActor()))
                .findFirst()
                .orElseThrow(NullPointerException::new);
        this.actor = new AbstractId(fleetSnapshot.getFleet().getId(), fleetSnapshot.getName());
        this.movementType = input.getMovementType();
        this.auraState = new AuraState(input.getAlignedAuraStates());
        this.lengthOnTrack = input.getLengthOnTrack();
    }

    @JsonIgnore
    public int getCombatRoundKey() {
        return combatRoundKey;
    }
}
