package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class ResourceDisplay extends VerticalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    public ResourceDisplay() {
        final Label miningFactorsTitle = new Label("Mining factors");
        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay();
            resourceElementDisplay.update(new EResourceAmountDTO(resourceType, BigDecimal.ZERO, null));
            componentMap.put(resourceType, resourceElementDisplay);
        });

        add(miningFactorsTitle);
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
