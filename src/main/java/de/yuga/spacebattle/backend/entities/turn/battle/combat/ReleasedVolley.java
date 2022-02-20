package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "releasedVolley")
@AttributeOverride(name = "id", column = @Column(name = "idReleasedVolley"))
public class ReleasedVolley extends CombatRoundKey {

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
     * The UUID of the damage dealer.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID damageDealer;

    /**
     * The amount of missiles in this salvo.
     */
    private int amountOfShots;

    /**
     * The distance of this shot.
     */
    @NotNull
    @Nonnull
    @Column(nullable = false, columnDefinition = "decimal(19, 0)")
    private BigDecimal initialDistance;


    public ReleasedVolley(@Nonnull final CombatRound combatRound,
                          @Nonnull final Fleet actor,
                          @Nonnull final Fleet target,
                          @Nonnull final UUID movingMissileSalvo,
                          @Nonnull final Integer missileAmount) {
        super(combatRound, ECombatPhase.ECombatSubPhase.MISSILE_MOVEMENT_PHASE);

    }

    public ReleasedVolley() {
    }

}
