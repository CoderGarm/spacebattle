package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Mission {

    @Nullable
    @JsonProperty
    @Schema(description = "The mission.")
    private Integer idMission;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The type of the mission.")
    private EMissionType missionType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The venue of this mission.")
    private Planet venue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The mission's individual war ships.")
    private List<WarShip> ships = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The mission's individual war ships.")
    private Set<Integer> warShipIDs = new HashSet<>();

    public Mission() {
    }

    public Mission(@Nonnull final de.yuga.spacebattle.backend.entities.turn.mission.Mission mission,
                   @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idMission = mission.getId();
        this.missionType = mission.getMissionType();
        this.venue = new Planet(mission.getVenue());
        this.ships = mission.getShips().stream().map(w -> new WarShip(w, w.getWarshipHealthState(), languageCode)).collect(Collectors.toList());
        this.warShipIDs = this.ships.stream().map(WarShip::getIdWarship).collect(Collectors.toSet());
    }

    @Nullable
    public Integer getIdMission() {
        return idMission;
    }

    @Nonnull
    public EMissionType getMissionType() {
        return missionType;
    }

    @Nonnull
    public Planet getVenue() {
        return venue;
    }

    @Nonnull
    public List<WarShip> getShips() {
        return ships;
    }

    @Nonnull
    public Set<Integer> getWarShipIDs() {
        return warShipIDs;
    }
}
