package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceCostAmountDTO;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceElementDisplay;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconAmountDTO;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconDisplaySingle;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class CostsDisplayDatasource {

    /**
     * Holds ever child elements of this display.
     */
    @Nonnull
    protected final Map<EResourceType, ResourceElementDisplay> resourceMap = new HashMap<>();

    @Nonnull
    protected final Map<EEducationType, CrewIconDisplaySingle> crewMap = new HashMap<>();

    @Nonnull
    private final EResolution resolution;

    public CostsDisplayDatasource(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        this.resolution = resolution;
    }

    /**
     * Sets the resource deposits to this view. If every resource deposit was added, call update.
     *
     * @param costs the costs to add
     */
    public void setValue(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(EDepositType.COSTS == costs.getSubType(), "costs must be costs!");

        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(resourceType -> {
            final long amount = costs.getResourceAmountByType(resourceType);
            if (amount == 0) {
                return;
            }
            ResourceElementDisplay resourceElementDisplay = resourceMap.get(resourceType);
            if (resourceElementDisplay == null) {
                resourceElementDisplay = new ResourceElementDisplay(resolution);
                resourceElementDisplay.addClassName("statistics-tight");
                resourceMap.put(resourceType, resourceElementDisplay);
            }
            resourceElementDisplay.setValue(new ResourceCostAmountDTO(resourceType, amount));
        });
        final CrewRequirementDTO crewRequirement = costs.getCrewRequirement();
        for (final EEducationType educationType : EEducationType.valuesOfWorkforce()) {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            if (amount == 0) {
                continue;
            }
            CrewIconDisplaySingle crewIconDisplaySingle = crewMap.get(educationType);
            if (crewIconDisplaySingle == null) {
                crewIconDisplaySingle = new CrewIconDisplaySingle(resolution);
                crewIconDisplaySingle.addClassName("statistics-tight");
                crewMap.put(educationType, crewIconDisplaySingle);
            }
            crewIconDisplaySingle.setValue(new CrewIconAmountDTO(educationType, amount));
        }
    }
}
