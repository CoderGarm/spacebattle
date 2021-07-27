package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "missileMotor")
@AttributeOverride(name = "id", column = @Column(name = "idMissileMotor"))
public class MissileMotor extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Column(nullable = false)
    private String typeName;

    /**
     * The duration which the missile engine can fire and accelerate the missile in seconds.
     */
    @Column(nullable = false)
    private int endurance;

    /**
     * The acceleration in m/s² which is set if using the engine.
     */
    @Column(nullable = false)
    private int acceleration;

    /**
     * Defines the capability of this weapon to penetrate the shield. todo
     * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
     */
    @Column(nullable = false)
    private int maneuverability;

    @Column(nullable = false)
    private int useCapacity;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(MissileMotor.class, EDepositType.COSTS);

    public MissileMotor() {
    }

    public MissileMotor(@Nonnull final String typeName,
                        final int endurance,
                        final int acceleration,
                        final int maneuverability,
                        final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");

        this.typeName = typeName;
        this.endurance = endurance;
        this.acceleration = acceleration;
        this.maneuverability = maneuverability;
        this.useCapacity = useCapacity;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public int getEndurance() {
        return endurance;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public int getManeuverability() {
        return maneuverability;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
