package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class ResourceDisplay extends HorizontalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    public ResourceDisplay(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        ViewHelper.setWidth(this, null);
        Map<EResourceType, BigDecimal> resources = resourceDeposit.getResources();

        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            final BigDecimal amount = resources.get(resourceType);
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay();
            resourceElementDisplay.update(new EResourceAmountWrapper(resourceType, amount, null));
            componentMap.put(resourceType, resourceElementDisplay);
        });

        ResourceElementDisplay[] components = new ResourceElementDisplay[componentMap.values().size()];
        components = componentMap.values().toArray(components);
        add(components);
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
                resourceElementDisplay.update(new EResourceAmountWrapper(resourceType, amount, null));
            }
        });
    }
}
