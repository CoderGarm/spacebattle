package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name, amount and the tickly difference yield if different from one.
 */
public class ResourceOutputDisplay extends VerticalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    public ResourceOutputDisplay() {
        final Label depositsTitle = new Label("Deposits");

        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay();
            resourceElementDisplay.update(new EResourceAmountWrapper(resourceType, BigDecimal.ZERO, BigDecimal.ZERO));
            componentMap.put(resourceType, resourceElementDisplay);
        });

        add(depositsTitle);
        for (int i = 0; i < EResourceType.values().length; i++) {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(EResourceType.values()[i]);
            add(resourceElementDisplay);
        }
    }

    @Nullable
    private BigDecimal getTickOutput(@Nonnull final Planet planet, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        final Construction construction = getConstruction(planet, resourceType);

        if (construction != null) {
            final BigDecimal resourceFactorByType = planet.getResourceFactors().getResourceAmountByType(resourceType);
            return construction.getTickOutput(resourceFactorByType);
        }
        return null;
    }

    @Nullable
    private Construction getConstruction(@Nonnull final Planet planet, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == resourceType)
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the display if called.
     *
     * @param planet the new input data
     */
    public void updateResourceDeposit(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        Map<EResourceType, BigDecimal> resources = resourceDeposit.getResources();
        Arrays.stream(EResourceType.values()).forEach(resourceType -> {
            ResourceElementDisplay resourceElementDisplay = componentMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final BigDecimal amount = resources.get(resourceType);
                final BigDecimal tickOutput = getTickOutput(planet, resourceType);
                resourceElementDisplay.update(new EResourceAmountWrapper(resourceType, amount, tickOutput));
            }
        });
    }
}
