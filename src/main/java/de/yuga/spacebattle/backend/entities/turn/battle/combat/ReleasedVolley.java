package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.BeamState;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "releasedVolley")
@AttributeOverride(name = "id", column = @Column(name = "idReleasedVolley"))
public class ReleasedVolley extends CombatRoundKey {

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTarget", nullable = false, updatable = false)
    private Fleet target;

    /**
     * The UUID of the damage dealer.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID damageDealer;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EWeaponType weaponType;

    /**
     * The amount of missiles in this salvo.
     */
    private int amountOfShots;

    /**
     * The distance of this shot.
     */
    @NotNull
    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance initialDistance;


    public ReleasedVolley() {
    }

    public ReleasedVolley(@Nonnull final BeamVolley volley) {
        super(volley.getCombatRound(), volley.getCombatSubPhase());

        final Map<WarShip, List<Long>> appliedDamage = volley.getAppliedDamage();
        final List<BeamState> firedShots = volley.getFiredShots();
        /*fixme notice the warship's potion of the salvo */

        this.weaponType = EWeaponType.BEAM;
        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.amountOfShots = firedShots.size();
        this.initialDistance = volley.getInitialDistance();
    }

    public ReleasedVolley(@Nonnull final MissileSalvo volley) {
        super(volley.getCombatRound(), volley.getCombatSubPhase());

        final Map<WarShip, List<Long>> appliedDamage = volley.getAppliedDamage();
        /*fixme notice the warship's potion of the salvo */

        this.weaponType = EWeaponType.MISSILE;
        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.amountOfShots = volley.getMissileSalvoHealthState().getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum();
        this.initialDistance = volley.getInitialDistance();
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
    public UUID getDamageDealer() {
        return damageDealer;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    public int getAmountOfShots() {
        return amountOfShots;
    }

    @Nonnull
    public Distance getInitialDistance() {
        return initialDistance;
    }
}
