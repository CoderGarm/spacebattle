package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EWarheadType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "warhead")
@AttributeOverride(name = "id", column = @Column(name = "idWarhead"))
public class Warhead extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Column(nullable = false)
    private String typeName;

    @Column(nullable = false)
    private long damageValue;

    /**
     * Defines the range of this weapon in meter.
     */
    @Column(nullable = false, columnDefinition = "decimal (19, 0)")
    private BigDecimal damageProjectionRange;

    /**
     * The way of damage projection.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWarheadType warheadType;

    @Column(nullable = false)
    private int useCapacity;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(Warhead.class, EDepositType.COSTS);

    public Warhead() {
    }

    public Warhead(@Nonnull final String typeName,
                   final int damageValue,
                   @Nonnull final BigDecimal damageProjectionRange,
                   @Nonnull final EWarheadType warheadType,
                   final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        this.typeName = typeName;
        this.damageValue = damageValue;
        this.damageProjectionRange = damageProjectionRange;
        this.warheadType = warheadType;
        this.useCapacity = useCapacity;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public BigDecimal getDamageProjectionRange() {
        return damageProjectionRange;
    }

    @Nonnull
    public EWarheadType getWarheadType() {
        return warheadType;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
