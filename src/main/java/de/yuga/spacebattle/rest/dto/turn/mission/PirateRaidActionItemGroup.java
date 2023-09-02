package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class PirateRaidActionItemGroup {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "What happens.")
    private EMissionType missionType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The venue of the action.")
    private Planet venue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The actions to report.")
    private List<PirateRaidActionItem> actionItems = new ArrayList<>();


    public PirateRaidActionItemGroup(@Nonnull final EMissionType missionType,
                                     @Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Planet planet,
                                     @Nonnull final List<MissionItem> items,
                                     @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(missionType, "missionType must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(items, "items must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        this.venue = new Planet(planet);
        this.missionType = missionType;
        this.actionItems = items.stream().map(i -> new PirateRaidActionItem(i, preferredLanguage)).collect(Collectors.toList());
    }
}
