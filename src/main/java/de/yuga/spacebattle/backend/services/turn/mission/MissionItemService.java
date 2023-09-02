package de.yuga.spacebattle.backend.services.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMissionItem;
import de.yuga.spacebattle.backend.repositories.turn.mission.ConvoyProtectionMissionItemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class MissionItemService {

    @Nonnull
    private final ConvoyProtectionMissionItemRepository convoyProtectionMissionItemRepository;

    public MissionItemService(@Nonnull final ConvoyProtectionMissionItemRepository convoyProtectionMissionItemRepository) {
        this.convoyProtectionMissionItemRepository = Preconditions.checkNotNull(convoyProtectionMissionItemRepository, "missionRepository must not be empty");
    }

    public ConvoyProtectionMissionItem save(@Nonnull final ConvoyProtectionMissionItem item) {
        Preconditions.checkNotNull(item, "item must not be empty");

        return convoyProtectionMissionItemRepository.save(item);
    }
}
