package de.yuga.spacebattle.backend.entities.turn.resources;


import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static de.yuga.spacebattle.backend.enums.EResourceType.HEAVY_METALS;
import static de.yuga.spacebattle.backend.enums.EResourceType.POPULATION;

/**
 * The planetary perquisites of natural resources.
 */
@NamedQueries({
        @NamedQuery(name = "MiningFactors.getAll", query = "SELECT p FROM MiningFactors p")
})
@Entity
@Table(name = "miningFactors")
@AttributeOverride(name = "id", column = @Column(name = "idMiningFactors"))
public class MiningFactors extends AbstractEntityKey {

    /**
     * The amount of resources and their type in order to know the occurrence of planetary resources.
     */
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "resourceType", updatable = false, length = 50)
    @MapKeyEnumerated(value = EnumType.STRING)
    @Column(name = "amount", columnDefinition = "decimal(19, 0)")
    @CollectionTable(name = "miningFactorsComposition", joinColumns = @JoinColumn(name = "idMiningFactors"))
    private final Map<EResourceType, Long> resources = new HashMap<>();

    public MiningFactors() {
        initialize();
    }

    public long getResourceAmountByType(@Nullable final EResourceType resourceType) {
        if (resources.containsKey(resourceType)) {
            return this.resources.get(resourceType);
        }
        return 0;
    }

    /**
     * Initializes the map and creates, if not happened before, the natural resources.
     */
    @Deprecated(since = "productive")
    private void initialize() {
        if (!resources.isEmpty()) {
            return;
        }
        for (EResourceType type : EResourceType.values()) {
            final long rand = ThreadLocalRandom.current().nextLong(29, 201);
            resources.put(type, rand);
        }
        // if the planet holds a lot of bad materials the population must be lowered - simple version
        final Long factorOfHeavyMetals = resources.get(HEAVY_METALS);
        final boolean moreThan7AtHeavy = factorOfHeavyMetals > 70;
        final Long factorOfPopulation = resources.get(POPULATION);
        final boolean moreThan7AtPop = factorOfPopulation > 70;
        if (moreThan7AtHeavy) {
            if (moreThan7AtPop) {
                resources.put(POPULATION, 70L);
            }
        }
    }
}
