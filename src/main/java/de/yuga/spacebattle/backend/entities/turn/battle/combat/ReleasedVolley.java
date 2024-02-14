package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
     * The initial amount of shots in this volley.
     */
    private int amountOfShots;


    public ReleasedVolley() {
    }

    public ReleasedVolley(@Nonnull final BeamVolley volley) {
        super(volley.getCombatRound(), ECombatPhase.ECombatSubPhase.BEAM_FIRE_PHASE);

        this.weaponType = EWeaponType.BEAM;
        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.amountOfShots = volley.getFiredShots().size();
    }

    public ReleasedVolley(@Nonnull final MissileSalvo volley) {
        super(volley.getCombatRound(), ECombatPhase.ECombatSubPhase.MISSILE_FIRE_PHASE);

        this.weaponType = EWeaponType.MISSILE;
        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.amountOfShots = volley.getMissileSalvoHealthState().getInitialAmountByType().values().stream().mapToInt(Integer::intValue).sum();
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
}
