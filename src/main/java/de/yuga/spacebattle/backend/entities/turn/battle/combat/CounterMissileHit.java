package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

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
     * The leftover amount of missiles of the given type in the salvo.
     */
    private int remainingMissiles;

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
    @OneToOne(optional = false)
    @JoinColumn(name = "idMissile", nullable = false, updatable = false)
    private Missile missile;

    public CounterMissileHit(@Nonnull final CombatRound combatRound,
                             @Nonnull final ECombatPhase.ECombatSubPhase combatPhase,
                             @Nonnull final Integer remainingMissiles,
                             @Nonnull final Fleet actor,
                             @Nonnull final Fleet target,
                             @Nonnull final UUID attackedMissileSalvo,
                             @Nonnull final Integer destroyedMissiles,
                             @Nonnull final Missile missile) {
        super(combatRound, combatPhase);

    }

    public CounterMissileHit() {
    }

}
