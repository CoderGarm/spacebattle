package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EHitArea;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "hitLog")
@AttributeOverride(name = "id", column = @Column(name = "idHitLog"))
public class HitLog extends CombatRoundKey {

    /**
     * The UUID of the damage dealer - which class of damage dealer hits, depends on the {@link CombatRoundKey#getCombatPhase()}.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID damageDealer;

    /**
     * The attacked warship.
     */
    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTarget", updatable = false, nullable = false)
    private WarShip warShip;

    /**
     * The string representation
     */
    @NotNull
    @Nonnull
    @Size(max = 500)
    private String warshipHealthState;

    /**
     * The applied damage.
     */
    private long damageValue;

    /**
     * The remaining hit points of the attacked part of the ship.
     */
    private int state;

    /**
     * The attacked part of the ship.
     */
    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EHitArea attackedPart;

    /**
     * If the ship is alive after damage.
     */
    private boolean isAlive;

    /**
     * If the ship is capable of staying in the battle after damage.
     */
    private boolean isFightingCapable;

    public HitLog(@Nonnull final CombatRound combatRound,
                  @Nonnull final ECombatPhase.ECombatSubPhase combatPhase) {
        super(combatRound, combatPhase);

    }

    public HitLog() {
    }

}
