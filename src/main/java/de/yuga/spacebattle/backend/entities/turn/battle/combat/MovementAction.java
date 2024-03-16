package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.AlignedAuraState;
import de.yuga.spacebattle.backend.combat.dto.AuraState;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.converter.AccelerationConverter;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.converter.VelocityConverter;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movementAction")
@AttributeOverride(name = "id", column = @Column(name = "idMovementAction"))
public class MovementAction extends CombatRoundKey {

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idManeuver", updatable = false)
    private de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver maneuver;

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EMovementType movementType;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alignedAuraStates", joinColumns = @JoinColumn(name = "idMovementAction"))
    private final Set<AlignedAuraState> alignedAuraStates = new HashSet<>();

    @Nonnull
    @NotNull
    @Convert(converter = DistanceConverter.class)
    private Distance lengthOnTrack;

    @Nonnull
    @NotNull
    @Convert(converter = VelocityConverter.class)
    private Velocity velocity;

    @Nonnull
    @NotNull
    @Convert(converter = AccelerationConverter.class)
    private Acceleration acceleration;

    public MovementAction() {
    }

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver maneuver,
                          @Nonnull final de.yuga.spacebattle.backend.combat.dto.MovementAction movementAction,
                          @Nonnull final AuraState auraState) {
        super(movementAction.getCombatRound(), movementAction.getCombatPhase());
        Preconditions.checkNotNull(auraState, "auraState must not be empty");

        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        this.actor = movementAction.getActor();
        this.movementType = movementAction.getMovementType();
        this.alignedAuraStates.addAll(auraState.getAlignedAuraStates().values());
        this.lengthOnTrack = movementAction.getLengthOnTrack();
        this.velocity = movementAction.getVelocity();
        this.acceleration = movementAction.getAcceleration();
    }

    @Nonnull
    public Maneuver getManeuver() {
        return maneuver;
    }

    public void replaceByPersistedManeuver(@Nonnull final Maneuver maneuver) {
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public EMovementType getMovementType() {
        return movementType;
    }

    @Nonnull
    public Set<AlignedAuraState> getAlignedAuraStates() {
        return alignedAuraStates;
    }

    @Nonnull
    public Distance getLengthOnTrack() {
        return lengthOnTrack;
    }

    @Nonnull
    public Velocity getVelocity() {
        return velocity;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }
}
