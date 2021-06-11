package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the deposit at the given planet.
 */
public class MiningFactorsVerticalDisplay extends VerticalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> componentMap = new HashMap<>();

    public MiningFactorsVerticalDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        for (final EResourceType resourceType : EResourceType.values()) {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay(resolution);
            resourceElementDisplay.addClassName("statistics-tight");
            resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, 0, null));
            componentMap.put(resourceType, resourceElementDisplay);
            add(resourceElementDisplay);
        }
    }

    /**
     * Updates the display if called.
     *
     * @param planet the new input data
     */
    public void setValue(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final MiningFactors miningFactors = planet.getMiningFactors();
        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = componentMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final long amount = miningFactors.getResourceAmountByType(resourceType);
                resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, amount, null));
            }
        });
        final long habitability = planet.getMiningFactors().getResourceAmountByType(EResourceType.POPULATION);
        final ResourceElementDisplay populationDisplay = componentMap.get(EResourceType.POPULATION);
        populationDisplay.setValue(new ResourceAmountDTO(EResourceType.POPULATION, habitability, null));
    }
}
