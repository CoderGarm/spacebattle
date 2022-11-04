package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.AccelerationConverter;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "missileMotor")
@AttributeOverride(name = "id", column = @Column(name = "idMissileMotor"))
public class MissileMotor extends HasCosts {

    /**
     * The duration which the missile engine can fire and accelerate the missile in seconds.
     */
    @Column(nullable = false)
    private int endurance;

    /**
     * The acceleration in gravity earth which is set if using the engine.
     */
    @Nonnull
    @NotNull
    @Convert(converter = AccelerationConverter.class)
    private Acceleration acceleration;

    /**
     * Defines the capability of this weapon to penetrate the shield. todo
     * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
     */
    @Column(nullable = false)
    private int maneuverability;

    @Column(nullable = false)
    private int useCapacity;

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    public MissileMotor() {
    }

    public MissileMotor(@Nonnull final String name,
                        @Nonnull final String description,
                        final int endurance,
                        @Nonnull final EHullType hullType,
                        @Nonnull final ETechLevel techLevel,
                        @Nonnull final Acceleration acceleration,
                        final int maneuverability,
                        final int useCapacity) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, endurance, MissileMotor.class);
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        this.endurance = endurance;
        this.acceleration = acceleration;
        this.maneuverability = maneuverability;
        this.useCapacity = useCapacity;
        this.hullType = hullType;
    }

    public int getEndurance() {
        return endurance;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }

    public int getManeuverability() {
        return maneuverability;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setEndurance(final int endurance) {
        this.endurance = endurance;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setAcceleration(@Nonnull final Acceleration acceleration) {
        this.acceleration = acceleration;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setManeuverability(final int maneuverability) {
        this.maneuverability = maneuverability;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setUseCapacity(final int useCapacity) {
        this.useCapacity = useCapacity;
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setHullType(@Nonnull final EHullType hullType) {
        this.hullType = hullType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof MissileMotor)) return false;

        final MissileMotor that = (MissileMotor) o;

        return new EqualsBuilder().append(id, that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
