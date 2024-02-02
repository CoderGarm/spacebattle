package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.AlignedAuraState;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class AuraState {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private List<de.yuga.spacebattle.rest.dto.turn.battle.combat.AlignedAuraState> auraStates = new ArrayList<>();

    public AuraState(@Nonnull final Set<AlignedAuraState> alignedAuraStates) {
        Preconditions.checkNotNull(alignedAuraStates, "alignedAuraStates must not be empty");

        this.auraStates.addAll(alignedAuraStates.stream().map(de.yuga.spacebattle.rest.dto.turn.battle.combat.AlignedAuraState::new).collect(Collectors.toList()));
    }
}
