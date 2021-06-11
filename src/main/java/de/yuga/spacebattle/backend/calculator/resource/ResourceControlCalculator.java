package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Runs the calculations for producing resources.
 */
public class ResourceControlCalculator {

    private ResourceControlCalculator() {
    }

    /**
     * Calculates the production per tick of this planet for the given resource type.<br>
     * <b>Attention:</b><br> The {@link EResourceType#POPULATION} will not be accepted.<br>
     * Use the {@link PopulationControlCalculator} instead.<br>
     * <br>
     * <b>Calculation rule:</b><br>
     * Collect all producing constructions and sum up their output by level.
     *
     * @param planet       the planet which should be calculated
     * @param resourceType the resource type
     * @return the pure production
     */
    @Nullable
    public static Long getTickOutput(@Nonnull final Planet planet, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkArgument(EResourceType.POPULATION != resourceType, "resourceType shouldn't be population!");

        final Set<Construction> constructionsByResource = planet.getConstructionByResource(resourceType);
        if (!constructionsByResource.isEmpty()) {
            // collecting all possible producing building
            final Map<EProductionCategory, List<Construction>> constructionMap = constructionsByResource.stream()
                    .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getProductionCategory(),
                            Collectors.mapping(Function.identity(), Collectors.toList())));
            // for normal resources only a pure production is possible
            final List<Construction> constructions = constructionMap.get(EProductionCategory.PRODUCE);
            // sum up all the output of the producing buildings
            return constructions.stream().map(TickOutputCalculator::getTickOutputByLevel).reduce(0L, Long::sum);
        }
        return null;
    }
}
