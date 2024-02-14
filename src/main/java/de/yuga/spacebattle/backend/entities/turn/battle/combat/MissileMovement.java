package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.dto.MotionProfile;
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
    @ManyToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    /**
     * The owner of the attacked salvo.
     */
    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
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
    private int missileAmount;

    /**
     * The amount of rounds which have to be passed before in range for a hit.
     */
    private int roundsToTravel;

    /**
     * The current position of the salvo.
     */
    @NotNull
    @Nonnull
    private Orbit position;

    /**
     * The current position of the target.
     */
    @NotNull
    @Nonnull
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordTarget"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordTarget"))
    private Orbit targetPosition;

    public MissileMovement() {
    }

    public MissileMovement(@Nonnull final MissileSalvo volley,
                           @Nonnull final MotionProfile motionProfile) {
        super(
                Preconditions.checkNotNull(motionProfile, "motionProfile must not be empty").getCombatRound(),
                ECombatPhase.ECombatSubPhase.MISSILE_MOVEMENT_PHASE
        );

        final KinematicInfo ki = motionProfile.getKinematicInfo();
        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.movingMissileSalvo = volley.getUuid();
        this.position = ki.getPosition().clone();
        this.missileAmount = volley.getMissileSalvoHealthState().getAmountByTypeAtEndOfCombatRound(motionProfile.getCombatRound()).values().stream().mapToInt(Integer::intValue).sum();
        this.targetPosition = volley.getTargetPosition().clone();
        this.roundsToTravel = volley.roundsTravelled();
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public Fleet getTarget() {
        return target;
    }

    @Nonnull
    public UUID getMovingMissileSalvo() {
        return movingMissileSalvo;
    }

    public int getMissileAmount() {
        return missileAmount;
    }

    public int getRoundsToTravel() {
        return roundsToTravel;
    }

    @Nonnull
    public Orbit getPosition() {
        return position;
    }

    @Nonnull
    public Orbit getTargetPosition() {
        return targetPosition;
    }
}
