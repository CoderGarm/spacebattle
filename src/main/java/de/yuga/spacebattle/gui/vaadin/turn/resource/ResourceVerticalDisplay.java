package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconAmountDTO;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconDisplaySingle;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the deposit at the given planet.
 */
public class ResourceVerticalDisplay extends VerticalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> resourceMap = new HashMap<>();

    @Nonnull
    private final Map<EEducationType, CrewIconDisplaySingle> crewMap = new HashMap<>();

    public ResourceVerticalDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        for (final EResourceType resourceType : EResourceType.valuesWithoutPopulation()) {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay(resolution);
            resourceElementDisplay.addClassName("statistics-tight");
            resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, 0, null));
            resourceMap.put(resourceType, resourceElementDisplay);
            add(resourceElementDisplay);
        }
        for (final EEducationType educationType : EEducationType.values()) {
            final CrewIconDisplaySingle crewIconDisplaySingle = new CrewIconDisplaySingle(resolution);
            crewIconDisplaySingle.addClassName("statistics-tight");
            crewIconDisplaySingle.setValue(new CrewIconAmountDTO(educationType, 0));
            crewMap.put(educationType, crewIconDisplaySingle);
            add(crewIconDisplaySingle);
        }
    }

    /**
     * Updates the display if called.
     *
     * @param resourceDeposit the new input data
     */
    public void updateResources(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = resourceMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final long amount = resourceDeposit.getResourceAmountByType(resourceType);
                resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, amount, null));
            }
        });
        final CrewRequirementDTO crewRequirement = resourceDeposit.getCrewRequirement();
        for (final EEducationType educationType : EEducationType.values()) {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            final CrewIconDisplaySingle crewIconDisplaySingle = crewMap.get(educationType);
            crewIconDisplaySingle.setValue(new CrewIconAmountDTO(educationType, amount));
        }
    }
}
