package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EHitArea;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @Column(columnDefinition = "boolean not null default true")
    private boolean isAlive;

    /**
     * If the ship is capable of staying in the battle after damage.
     */
    @Column(columnDefinition = "boolean not null default true")
    private boolean isFightingCapable;

    public HitLog() {
    }

    public HitLog(@Nonnull final UUID damageDealerId,
                  @Nonnull final de.yuga.spacebattle.backend.combat.dto.HitLog hitLog,
                  @Nonnull final ECombatPhase.ECombatSubPhase combatSubPhase) {
        super(hitLog.getCombatRound(), combatSubPhase);
        Preconditions.checkNotNull(damageDealerId, "damageDealerId shouldn't be null!");

        this.damageDealer = damageDealerId;
        final WarshipHealthState warshipHealthState = hitLog.getTargetHealthState();
        this.warShip = warshipHealthState.getWarShip();
        this.damageValue = hitLog.getDamageValue();
        this.state = hitLog.getState();
        this.attackedPart = hitLog.getAttackedPart();
        this.isAlive = hitLog.isAlive();
        this.isFightingCapable = hitLog.isFightingCapable();
    }

    @Nonnull
    public UUID getDamageDealer() {
        return damageDealer;
    }

    @Nonnull
    public WarShip getWarShip() {
        return warShip;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public int getState() {
        return state;
    }

    @Nonnull
    public EHitArea getAttackedPart() {
        return attackedPart;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isFightingCapable() {
        return isFightingCapable;
    }
}
