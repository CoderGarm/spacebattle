package de.yuga.spacebattle.backend.entities;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * table should be incremented and mapped by owning entities
 * - subType 0 is resourcefactor only for planets
 * - subType 1 is deposit in ships or planets
 * - subType 2 are costs
 */
@NamedQueries({
        @NamedQuery(name = "ResourceDeposit.getAll", query = "SELECT p FROM ResourceDeposit p")
})
@Entity
@Table(name = "resourceDeposit")
@AttributeOverride(name = "id", column = @Column(name = "idResourceDeposit"))
public class ResourceDeposit extends AbstractEntityKey {

    // todo discuss rounding mode - scale 2 not really necessary if planet's resource prequisites is calculated at another way
    public final static MathContext mathContext = new MathContext(2, RoundingMode.DOWN);

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "type", updatable = false, length = 50)
    @MapKeyEnumerated(value = EnumType.STRING)
    @Column(name = "amount", scale = 2)
    @CollectionTable(name = "rescources", joinColumns = @JoinColumn(name = "idResourceDeposit"))
    private Map<EResourceType, BigDecimal> resources = new HashMap<>();

    @Nonnull
    @NotNull(message = "SubType must be defined.")
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private EResourceSubType subType = EResourceSubType.DEFAULT; // strange validation at instanziating proxies through spring

    public ResourceDeposit() {
    }

    public ResourceDeposit(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        this.subType = resourceDeposit.getSubType();
        this.resources = new HashMap<>(resourceDeposit.getResources());

    }

    public ResourceDeposit(@Nonnull final EResourceSubType subType) {
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        this.subType = subType;
        initialize();
    }

    @Nonnull
    public Map<EResourceType, BigDecimal> getResources() {
        return resources;
    }

    public BigDecimal getResourceAmountByType(EResourceType resourceType) {
        if (resources.containsKey(resourceType)) {
            return this.resources.get(resourceType);
        }
        return BigDecimal.ZERO;
    }

    @Nonnull
    public EResourceSubType getSubType() {
        return subType;
    }

    public void setSubType(@Nonnull final EResourceSubType subType) {
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        this.subType = subType;
    }

    public void updateResource(@Nonnull final EResourceType rescourceType, @Nonnull final BigDecimal amount) {
        Preconditions.checkNotNull(rescourceType, "rescourceType shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        BigDecimal value;
        if (resources.containsKey(rescourceType)) {
            value = this.resources.get(rescourceType).add(amount);
        } else {
            value = amount;
        }
        this.resources.put(rescourceType, value.setScale(mathContext.getPrecision(), mathContext.getRoundingMode()));
    }

    /**
     * Initializes the map and creates, if not happended before, the natural resources.
     */
    private void initialize() {
        if (!resources.isEmpty()) {
            return;
        }

        for (EResourceType type : EResourceType.values()) {
            double rand = 0;
            switch (subType) {
                case COSTS:
                    rand = ThreadLocalRandom.current().nextDouble(10, 51);
                    break;
                case DEPOSITS:
                    // stay zero but to play games at start
                    rand = ThreadLocalRandom.current().nextDouble(1000, 5100);
                    break;
                case MININGFACTORS:
                    rand = ThreadLocalRandom.current().nextDouble(29, 201);
                    break;
            }
            resources.put(type, new BigDecimal(rand, mathContext));
        }
    }
}
