package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMarker;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Maneuver {

    @JsonProperty
    @Schema(required = true, description = "The round and phase information.")
    private int combatRoundKey;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of the maneuver.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planned end.")
    private CombatRound designatedEnd;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The real end.")
    private CombatRound end;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private FleetMarker actor;

    @Nullable
    @JsonProperty
    @Schema(description = "The UUID of the missile salvo if this maneuver is for it.")
    private UUID missileSalvo;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet which acts.")
    private List<ManeuverElement> maneuverElements = new ArrayList<>();

    public Maneuver(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver input,
                    @Nonnull final Set<FleetSnapshot> participatingFleets) {
        Preconditions.checkNotNull(input, "input shouldn't be null!");
        Preconditions.checkNotNull(participatingFleets, "participatingFleets must not be empty");

        this.combatRoundKey = input.getCombatRound().getNo();
        this.designatedEnd = new CombatRound(input.getDesignatedEnd());
        this.end = new CombatRound(input.getEnd());
        final FleetSnapshot fleetSnapshot = participatingFleets.stream()
                .filter(snap -> snap.getFleet().equals(input.getActor()))
                .findFirst()
                .orElseThrow(NullPointerException::new);
        this.actor = new FleetMarker(fleetSnapshot);
        this.name = input.getName();
        this.missileSalvo = input.getMovingMissileSalvo();
        this.maneuverElements.addAll(input.getManeuverElements().stream().map(ManeuverElement::new).collect(Collectors.toList()));
    }

}
