package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.misc.HasEffectValue;
import de.yuga.spacebattle.backend.entities.misc.HasNamedTechLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import jakarta.persistence.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * There will be only one propulsion type and it will be taken for FTL- and sub light-travelling.
 * The propulsion is dual-use for both in every ship.
 */
@NamedQueries({
        @NamedQuery(name = "Propulsion.getAll", query = "SELECT a FROM Propulsion a"),
        @NamedQuery(name = "Propulsion.getAllByResearches",
                query = "SELECT a FROM Propulsion a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "propulsion")
@AttributeOverride(name = "id", column = @Column(name = "idPropulsion"))
public class Propulsion extends HasNamedTechLevel implements HasEffectValue {

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

    /**
     * If this propulsion module provides the ability to travel faster than light.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHyperBand hyperBand;

    /**
     * If this propulsion module is for military or civil purposes.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechnologyType technologyType;

    /**
     * The percentage of the parent's module cost which represents the costs of 'this'.
     */
    private int costsPercentage;

    private int effectValue;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final NamedTechLevel baseModule,
                      @Nonnull final String technicalTypeName,
                      final int unlockedThroughLevel,
                      final int effectValue,
                      final int costsPercentage,
                      @Nonnull final EHyperBand hyperBand,
                      @Nonnull final ETechnologyType technologyType) {
        super(baseModule, unlockedThroughLevel, technicalTypeName);
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

        this.hyperBand = hyperBand;
        this.technologyType = technologyType;
        this.costsPercentage = costsPercentage;
        this.effectValue = effectValue;
    }

    @Nonnull
    public EHyperBand getHyperBand() {
        return hyperBand;
    }

    @Nonnull
    public ETechnologyType getTechnologyType() {
        return technologyType;
    }

    public boolean isFtlCapable() {
        return hyperBand != EHyperBand.NONE;
    }

    public int getCostsPercentage() {
        return costsPercentage;
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }


    private BigDecimal getPercentValue(final BigDecimal baseValue) {
        if (baseValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(costsPercentage)
                .multiply(baseValue, new MathContext(8, RoundingMode.HALF_UP))
                .divide(BigDecimal.valueOf(100), new MathContext(8, RoundingMode.HALF_UP));
    }

    public Mass getTonnage(@Nonnull final Mass tonnage) {
        Preconditions.checkNotNull(tonnage, "tonnage must not be empty");

        final BigDecimal baseValue = tonnage.getCoordinateInMetric(EMassMetric.T);
        final BigDecimal result = getPercentValue(baseValue);
        return new Mass(result, EMassMetric.T);
    }

    public ResourceDeposit getCosts(@Nonnull final Mass tonnage) {
        Preconditions.checkNotNull(tonnage, "tonnage must not be empty");

        final BigDecimal massToPay = BigDecimal.valueOf(costsPercentage)
                .multiply(tonnage.getCoordinate(), MC)
                .divide(BigDecimal.valueOf(100), new MathContext(8, RoundingMode.HALF_UP));

        final ETechLevel techLevel = getTechLevel();
        return ResourceDepositInitializerCalculator.getCostsForTonnage(techLevel, new Mass(massToPay.add(tonnage.getCoordinate()), tonnage.getMassMetric()));
    }
}
