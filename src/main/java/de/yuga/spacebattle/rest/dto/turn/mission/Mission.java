package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMission;
import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.TradeContract;
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
    @Schema(required = true, description = "When the mission was started.")
    private Tick started;

    @Nullable
    @JsonProperty
    @Schema(description = "The venue of this mission.")
    private Planet venue;

    @Nullable
    @JsonProperty
    @Schema(description = "The target when this mission is a convoy protection.")
    private Integer idTradedResource;

    @Nullable
    @JsonProperty
    @Schema(description = "The target when this mission is a convoy protection.")
    private TradeContract tradeContract;

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
        this.started = new Tick(mission.getStarted());
        this.venue = mission instanceof PirateHuntMission ? new Planet(((PirateHuntMission) mission).getVenue()) : null;
        this.idTradedResource = mission instanceof ConvoyProtectionMission ? ((ConvoyProtectionMission) mission).getProtectedTrade().getId() : null;
        this.tradeContract = mission instanceof ConvoyProtectionMission ? new TradeContract(((ConvoyProtectionMission) mission).getProtectedTrade()) : null;
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

    @Nullable
    public Planet getVenue() {
        return venue;
    }

    @Nullable
    public Integer getIdTradedResource() {
        return idTradedResource;
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
