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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class CostsDisplay extends VerticalLayout {

    /**
     * Holds ever child elements of this display.
     */
    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    /**
     * Holds the summarized amount of resources.
     */
    @Nonnull
    private final Map<EResourceType, BigDecimal> resources;

    public CostsDisplay() {
        final Label title = new Label("Costs");

        resources = Arrays.stream(EResourceType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));

        update();
        add(title);
        for (int i = 0; i < EResourceType.values().length; i++) {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(EResourceType.values()[i]);
            add(resourceElementDisplay);
        }
    }

    /**
     * Clears the added resources to zero.
     */
    public void clear() {
        Arrays.stream(EResourceType.values()).forEach(resourceType -> resources.put(resourceType, BigDecimal.ZERO));
    }

    /**
     * Updates the display.
     */
    public void update() {
        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(resourceType);
            final BigDecimal amount = resources.get(resourceType);
            if (resourceElementDisplay == null) {
                resourceElementDisplay = new ResourceElementDisplay();
                componentMap.put(resourceType, resourceElementDisplay);
            }
            resourceElementDisplay.update(new EResourceAmountWrapper(resourceType, amount, null));
        });

        for (int i = 0; i < EResourceType.values().length; i++) {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(EResourceType.values()[i]);
            add(resourceElementDisplay);
        }
    }

    /**
     * Add resource deposits to this view. If every resource deposit was added, call update.
     *
     * @param resourceDeposit the costs to add
     */
    public void addCosts(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            BigDecimal toAdd = resourceDeposit.getResourceAmountByType(resourceType);
            BigDecimal current = resources.get(resourceType);
            resources.put(resourceType, current.add(toAdd));
        });
    }
}
