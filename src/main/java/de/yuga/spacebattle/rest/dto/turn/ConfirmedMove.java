package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMarker;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ConfirmedMove {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleets which are confirmed for the move.")
    private List<FleetMarker> attendants = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The move.")
    private Move move;

    @JsonProperty
    @Schema(required = true, description = "The move identifier.")
    private int moveHash;

    public ConfirmedMove() {
    }

    public ConfirmedMove(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.Move> moves) {
        Preconditions.checkNotNull(moves, "moves must not be empty");

        final Set<FleetOrbit> destinations = moves.stream().map(de.yuga.spacebattle.backend.entities.turn.Move::getDestinationOrbit).collect(Collectors.toSet());
        final Set<FleetOrbit> origins = moves.stream().map(de.yuga.spacebattle.backend.entities.turn.Move::getOriginOrbit).collect(Collectors.toSet());
        Preconditions.checkArgument(destinations.size() == 1 && origins.size() == 1, "A confirmed move must have bo only a single set or places.");

        this.attendants = moves.stream().map(de.yuga.spacebattle.backend.entities.turn.Move::getFleet).map(FleetMarker::new).collect(Collectors.toList());
        final de.yuga.spacebattle.backend.entities.turn.Move first = moves.stream().findFirst().orElseThrow(NullPointerException::new);
        this.move = new Move(Objects.requireNonNull(first));
        this.moveHash = first.getMoveHash();
    }
}
