package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "missileMovement")
@AttributeOverride(name = "id", column = @Column(name = "idMissileMovement"))
public class MissileMovement extends CombatRoundKey {

    /**
     * The source of the electronic warfare.
     */
    @NotNull
    @Nonnull
    @OneToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    /**
     * The owner of the attacked salvo.
     */
    @NotNull
    @Nonnull
    @OneToOne(optional = false)
    @JoinColumn(name = "idTarget", nullable = false, updatable = false)
    private Fleet target;

    /**
     * The UUID of the moving {@link MissileSalvo}.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID movingMissileSalvo;

    /**
     * The amount of missiles in this salvo.
     */
    @NotNull
    @Nonnull
    private Integer missileAmount;

    /**
     * The amount of rounds which have to be passed before in range for a hit.
     */
    @NotNull
    @Nonnull
    private Integer roundsToTravel;

    /**
     * The current position of the salvo.
     */
    @NotNull
    @Nonnull
    private Orbit position;

    /**
     * The position of the salvo in the last round.
     */
    @NotNull
    @Nonnull
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordLast", columnDefinition = "decimal(19, 0)"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordLast", columnDefinition = "decimal(19, 0)"))
    private Orbit lastPosition;

    /**
     * The current position of the target.
     */
    @NotNull
    @Nonnull
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordTarget", columnDefinition = "decimal(19, 0)"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordTarget", columnDefinition = "decimal(19, 0)"))
    private Orbit targetPosition;


    public MissileMovement(@Nonnull final CombatRound combatRound,
                           @Nonnull final Fleet actor,
                           @Nonnull final Fleet target,
                           @Nonnull final UUID movingMissileSalvo,
                           @Nonnull final Integer missileAmount) {
        super(combatRound, ECombatPhase.ECombatSubPhase.MISSILE_MOVEMENT_PHASE);

    }

    public MissileMovement() {
    }

}
