package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ManeuverElement {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The parent maneuver.")
    private AbstractId maneuver;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Point one.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit p1;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Control Point one.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit cp1;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Control point two.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit cp2;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Point two.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit p2;

    @JsonProperty
    @Schema(required = true, description = "Represented as percent value.")
    private int partOfManeuver;

    @JsonProperty
    @Schema(required = true, description = "The number of planned execution as part of the parent maneuver.")
    private int sequenceNo;

    public ManeuverElement() {
    }

    public ManeuverElement(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.ManeuverElement maneuverElement) {
        Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");

        this.maneuver = new AbstractId(maneuverElement.getManeuver(), maneuverElement.getManeuver().getName());
        this.p1 = getOrbit(maneuverElement.getP1());
        this.cp1 = getOrbit(maneuverElement.getCp1());
        this.cp2 = getOrbit(maneuverElement.getCp2());
        this.p2 = getOrbit(maneuverElement.getP2());
        this.partOfManeuver = maneuverElement.getPartOfManeuver();
        this.sequenceNo = maneuverElement.getSequenceNo();
    }

    @Nonnull
    @JsonIgnore
    private Orbit getOrbit(final de.yuga.spacebattle.backend.entities.orbitals.Orbit position) {
        Preconditions.checkNotNull(position, "position must not be empty");

        return new Orbit(position);
    }

}
