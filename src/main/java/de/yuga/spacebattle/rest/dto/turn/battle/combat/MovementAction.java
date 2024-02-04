package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMarker;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Set;

@Schema(description = ".")
public class MovementAction {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private CombatRoundKey combatRoundKey;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMarker actor;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The selected movement option for this action.")
    private EMovementType movementType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The starting position for this movement.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit origin;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The next step to the targeted position.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit interimDestination;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The aura range states.")
    private AuraState auraState;

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction input,
                          @Nonnull final String languageCode,
                          @Nonnull final Set<FleetSnapshot> participatingFleets) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");
        Preconditions.checkNotNull(participatingFleets, "participatingFleets must not be empty");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        final FleetSnapshot fleetSnapshot = participatingFleets.stream()
                .filter(snap -> snap.getFleet().equals(input.getActor()))
                .findFirst()
                .orElseThrow(NullPointerException::new);
        this.actor = new FleetMarker(fleetSnapshot);
        this.movementType = input.getMovementType();
        this.origin = new Orbit(input.getOrigin());
        this.interimDestination = new Orbit(input.getInterimDestination());
        this.auraState = new AuraState(input.getAlignedAuraStates());
    }

    @Nonnull
    @JsonIgnore
    public CombatRoundKey getCombatRoundKey() {
        return combatRoundKey;
    }
}
