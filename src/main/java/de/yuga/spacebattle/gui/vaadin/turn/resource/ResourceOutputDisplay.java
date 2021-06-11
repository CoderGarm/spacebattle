package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceControlCalculator;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.SimpleLabelWithCaption;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconOutputDTO;
import de.yuga.spacebattle.gui.vaadin.turn.resource.crew.CrewIconOutputDisplaySingle;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name, amount and the tickly difference yield if different from one.
 */
@CssImport("./styles/views/main/details/resource-output.css")
public class ResourceOutputDisplay extends VerticalLayout {

    @Nonnull
    private final Map<EResourceType, ResourceElementDisplay> resourceMap = new HashMap<>();

    @Nonnull
    private final SimpleLabelWithCaption populationCapacityIndicator = new SimpleLabelWithCaption("Pop. / (Capacity)");

    @Nonnull
    private final Map<EEducationType, CrewIconOutputDisplaySingle> crewMap = new HashMap<>();

    public ResourceOutputDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        for (final EResourceType resourceType : EResourceType.valuesWithoutPopulation()) {
            final ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay(resolution);
            resourceElementDisplay.addClassName("statistics-tight");
            resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, 0, 0L));
            resourceMap.put(resourceType, resourceElementDisplay);
            add(resourceElementDisplay);
        }
        populationCapacityIndicator.addClassName("statistics-tight");
        add(populationCapacityIndicator);
        for (final EEducationType educationType : EEducationType.values()) {
            final CrewIconOutputDisplaySingle crewIconDisplaySingle = new CrewIconOutputDisplaySingle(resolution);
            crewIconDisplaySingle.addClassName("statistics-tight");
            crewIconDisplaySingle.setValue(new CrewIconOutputDTO(educationType, 0L, null));
            crewMap.put(educationType, crewIconDisplaySingle);
            add(crewIconDisplaySingle);
        }
    }

    /**
     * Updates the display if called.
     *
     * @param planet the new input data
     */
    public void setValue(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(resourceType -> {
            final ResourceElementDisplay resourceElementDisplay = resourceMap.get(resourceType);
            if (resourceElementDisplay != null) {
                final long amount = resourceDeposit.getResourceAmountByType(resourceType);
                final Long tickOutput = ResourceControlCalculator.getTickOutput(planet, resourceType);
                resourceElementDisplay.setValue(new ResourceAmountDTO(resourceType, amount, tickOutput));
            }
        });

        Long newborns = PopulationControlCalculator.getTickOutputForPopulation(planet);
        final CrewRequirementDTO crewRequirement = planet.getResourceDeposit().getCrewRequirement();
        final long sumOfPopulation = crewRequirement.getSumOfPopulation();
        final long populationCapacity = planet.getPopulationCapacity();
        populationCapacityIndicator.setValue(sumOfPopulation + " (" + populationCapacity + ")");

        for (final EEducationType educationType : EEducationType.values()) {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            final CrewIconOutputDisplaySingle crewIconDisplaySingle = crewMap.get(educationType);
            if (EEducationType.NONE != educationType) {
                // only NONE can grow
                newborns = null;
            }
            crewIconDisplaySingle.setValue(new CrewIconOutputDTO(educationType, amount, newborns));
        }
    }
}
