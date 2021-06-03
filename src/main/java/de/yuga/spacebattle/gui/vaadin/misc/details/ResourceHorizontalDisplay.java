package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
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
            resourceElementDisplay.update(new EResourceAmountDTO(resourceType, BigDecimal.ZERO, null));
            componentMap.put(resourceType, resourceElementDisplay);
        });

        for (int i = 0; i < EResourceType.values().length; i++) {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(EResourceType.values()[i]);
            add(resourceElementDisplay);
        }
    }

    /**
     * Updates the display if called.
     *
     * @param resourceDeposit the new input data
     */
    public void updateResourceDeposit(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        Map<EResourceType, BigDecimal> resources = resourceDeposit.getResources();
        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final BigDecimal amount = resources.get(resourceType);
                resourceElementDisplay.update(new EResourceAmountDTO(resourceType, amount, null));
            }
        });
    }
}
