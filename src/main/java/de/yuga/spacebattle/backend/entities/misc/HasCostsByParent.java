package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

@MappedSuperclass
public class HasCostsByParent extends HasNamedTechLevel implements HasEffectValue {

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

    /**
     * The percentage of the parent's module cost which represents the costs of 'this'.
     */
    private int costsPercentage;

    private int effectValue;

    public HasCostsByParent() {
    }

    public HasCostsByParent(@Nonnull final NamedTechLevel baseModule,
                            @Nonnull final String technicalTypeName,
                            @Nonnull final Research unlockedThrough,
                            final int costsPercentage,
                            final int effectValue) {
        super(baseModule, unlockedThrough, technicalTypeName);

        this.costsPercentage = costsPercentage;
        this.effectValue = effectValue;
    }

    public int getCostsPercentage() {
        return costsPercentage;
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }

    private int getPercentValue(final int baseValue) {
        if (baseValue == 0) {
            return 0;
        }
        return BigDecimal.valueOf(costsPercentage)
                .multiply(BigDecimal.valueOf(baseValue), MC)
                .divide(BigDecimal.valueOf(100), new MathContext(8, RoundingMode.HALF_UP)).intValue();
    }

    private long getPercentValue(final long baseValue) {
        if (baseValue == 0) {
            return 0;
        }
        return BigDecimal.valueOf(costsPercentage)
                .multiply(BigDecimal.valueOf(baseValue), new MathContext(8, RoundingMode.HALF_UP))
                .divide(BigDecimal.valueOf(100), new MathContext(8, RoundingMode.HALF_UP)).longValue();
    }

    public int getUseCapacity(@Nonnull final Hull hull) {
        Preconditions.checkNotNull(hull, "hull must not be empty");

        return getPercentValue(hull.getConstructionCapacity());
    }

    public ResourceDeposit getCosts(@Nonnull final Hull hull) {
        Preconditions.checkNotNull(hull, "hull must not be empty");

        final ResourceDeposit result = new ResourceDeposit(EDepositType.COSTS);
        EEducationType.WORKFORCE.forEach(type -> result.setAbsolutePopulation(type, getPercentValue(hull.getCosts().getCrewAmountByType(type))));
        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(type -> result.setAbsoluteResourceValue(type, getPercentValue(hull.getCosts().getResourceAmountByType(type))));
        return result;
    }
}
