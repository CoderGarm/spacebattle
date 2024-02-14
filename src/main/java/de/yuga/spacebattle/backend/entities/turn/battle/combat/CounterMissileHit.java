package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionProfile;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "counterMissileHit")
@AttributeOverride(name = "id", column = @Column(name = "idCounterMissileHit"))
public class CounterMissileHit extends CombatRoundKey {

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
     * The UUID of the attacked {@link MissileSalvo}.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID attackedMissileSalvo;

    /**
     * The amount of destroyed missiles.
     */
    private int destroyedMissiles;

    /**
     * The attacked missile type as part of the salvo.
     */
    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idMissile", nullable = false, updatable = false)
    private Missile missile;


    public CounterMissileHit(@Nonnull final MissileSalvo volley,
                             @Nonnull final MissileAmmunitionProfile ammunitionProfile,
                             @Nonnull final Missile missile,
                             final int destroyedMissiles) {
        super(
                Preconditions.checkNotNull(ammunitionProfile, "ammunitionProfile must not be empty").getCombatRound(),
                Preconditions.checkNotNull(ammunitionProfile, "ammunitionProfile must not be empty").getCombatSubPhase()
        );
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.attackedMissileSalvo = volley.getUuid();
        this.missile = missile;
        this.destroyedMissiles = destroyedMissiles;
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
    public UUID getAttackedMissileSalvo() {
        return attackedMissileSalvo;
    }

    public int getDestroyedMissiles() {
        return destroyedMissiles;
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }
}
