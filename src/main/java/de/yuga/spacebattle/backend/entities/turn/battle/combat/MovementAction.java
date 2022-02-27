package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

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

    /**
     * The point of the targeted position.
     */
    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordDestination"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordDestination"))
    private Orbit destination;

    public MovementAction() {
    }

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.combat.dto.MovementAction movementAction) {
        super(movementAction.getCombatRound(), movementAction.getCombatPhase());

        this.actor = movementAction.getActor();
        this.movementType = movementAction.getMovementType();
        this.origin = movementAction.getOrigin().clone();
        this.interimDestination = movementAction.getInterimDestination().clone();
        this.destination = movementAction.getDestination().clone();
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
    public Orbit getDestination() {
        return destination;
    }
}
