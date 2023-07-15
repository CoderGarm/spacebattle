package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EMissionType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = ".")
public class MissionReport {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The by-venue grouped results.")
    private List<MissionActionItemGroup> actionItemGroups = new ArrayList<>();

    public MissionReport(@Nonnull final List<MissionItem> missionItems, @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(missionItems, "missionItems must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final Map<StarSystem, List<MissionItem>> itemsBySystem = missionItems.stream().collect(Collectors.groupingBy(c -> c.getTarget().getSystem(),
                Collectors.mapping(Function.identity(), Collectors.toList())));

        itemsBySystem.forEach((starSystem, items) -> {
            final Map<EMissionType, List<MissionItem>> collect = items.stream()
                    .collect(Collectors.groupingBy(MissionItem::geteMissionType,
                            Collectors.mapping(Function.identity(), Collectors.toList())));

            collect.forEach((missionType, items1) -> {

                items1.stream()
                        .collect(Collectors.groupingBy(MissionItem::getTarget,
                                Collectors.mapping(Function.identity(), Collectors.toList())))
                        .forEach((planet, itemsPerPlanet) -> actionItemGroups.add(new MissionActionItemGroup(missionType, planet, itemsPerPlanet, preferredLanguage)));
            });
        });

    }
}
