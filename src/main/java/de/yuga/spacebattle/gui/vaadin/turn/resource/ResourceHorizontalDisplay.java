package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class ResourceHorizontalDisplay extends HorizontalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    public ResourceHorizontalDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay(resolution);
            resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, 0, null));
            componentMap.put(resourceType, resourceElementDisplay);
        });

        for (int i = 0; i < EResourceType.values().length; i++) {
            final ResourceElementDisplay resourceElementDisplay = componentMap.get(EResourceType.values()[i]);
            add(resourceElementDisplay);
        }
    }

    /**
     * Updates the display if called.
     *
     * @param miningFactors the new input data
     */
    public void updateResources(@Nonnull final MiningFactors miningFactors) {
        Preconditions.checkNotNull(miningFactors, "miningFactors shouldn't be null!");

        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = componentMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final long amount = miningFactors.getResourceAmountByType(resourceType);
                resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, amount, null));
            }
        });
    }
}
