package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

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
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordInterimDestination", columnDefinition = "decimal(19, 0)"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordInterimDestination", columnDefinition = "decimal(19, 0)"))
    private Orbit interimDestination;

    /**
     * The point of the targeted position.
     */
    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordDestination", columnDefinition = "decimal(19, 0)"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordDestination", columnDefinition = "decimal(19, 0)"))
    private Orbit destination;

    public MovementAction() {
    }

    public MovementAction(@Nonnull final CombatRound combatRound,
                          @Nonnull final Fleet actor,
                          @Nonnull final EMovementType movementType,
                          @Nonnull final Orbit origin,
                          @Nonnull final Orbit interimDestination,
                          @Nonnull final Orbit destination) {
        super(combatRound, ECombatPhase.ECombatSubPhase.MOVEMENT_PHASE);

    }

}
