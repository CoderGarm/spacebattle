package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "missileMovement")
@AttributeOverride(name = "id", column = @Column(name = "idMissileMovement"))
public class MissileMovement extends CombatRoundKey {

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idManeuver", updatable = false)
    private de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver maneuver;

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

    @Nonnull
    @NotNull
    @Convert(converter = DistanceConverter.class)
    private Distance lengthOnTrack;

    public MissileMovement() {
    }

    public MissileMovement(@Nonnull final MissileSalvo volley,
                           @Nonnull final CourseOrderElement courseOrderElement) {
        super(
                Preconditions.checkNotNull(courseOrderElement, "courseOrderElement must not be empty").getCombatRound(),
                ECombatPhase.ECombatSubPhase.MISSILE_MOVEMENT_PHASE
        );

        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.movingMissileSalvo = volley.getUuid();
        this.maneuver = new Maneuver(volley.getManeuver());
        this.lengthOnTrack = courseOrderElement.getLengthOnTrack();
        this.missileAmount = volley.getMissileSalvoHealthState().getAmountByTypeAtEndOfCombatRound(courseOrderElement.getCombatRound()).values().stream().mapToInt(Integer::intValue).sum();
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

    @Nonnull
    public Distance getLengthOnTrack() {
        return lengthOnTrack;
    }
}
