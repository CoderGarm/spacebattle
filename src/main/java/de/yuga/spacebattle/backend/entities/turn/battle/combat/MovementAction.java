package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.AlignedAuraState;
import de.yuga.spacebattle.backend.combat.dto.AuraState;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movementAction")
@AttributeOverride(name = "id", column = @Column(name = "idMovementAction"))
public class MovementAction extends CombatRoundKey {

    /**
     * The source of the salvo.
     */
    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    /**
     * The selected movement option for this action.
     */
    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EMovementType movementType;

    /**
     * The starting position for this movement.
     */
    @NotNull
    @Nonnull
    @Embedded
    private Orbit origin;

    /**
     * The next step to the targeted position.
     */
    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordInterimDestination"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordInterimDestination"))
    private Orbit interimDestination;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alignedAuraStates", joinColumns = @JoinColumn(name = "idMovementAction"))
    private final Set<AlignedAuraState> alignedAuraStates = new HashSet<>();

    public MovementAction() {
    }

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.combat.dto.MovementAction movementAction, @Nonnull final AuraState auraState) {
        super(movementAction.getCombatRound(), movementAction.getCombatPhase());
        Preconditions.checkNotNull(auraState, "auraState must not be empty");

        this.actor = movementAction.getActor();
        this.movementType = movementAction.getMovementType();
        this.origin = movementAction.getOrigin().clone();
        this.interimDestination = movementAction.getInterimDestination().clone();
        this.alignedAuraStates.addAll(auraState.getAlignedAuraStates().values());
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
    public Orbit getOrigin() {
        return origin;
    }

    @Nonnull
    public Orbit getInterimDestination() {
        return interimDestination;
    }

    @Nonnull
    public Set<AlignedAuraState> getAlignedAuraStates() {
        return alignedAuraStates;
    }
}
