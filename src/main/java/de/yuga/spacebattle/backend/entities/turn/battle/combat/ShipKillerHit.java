package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shipKillerHit")
@AttributeOverride(name = "id", column = @Column(name = "idShipKillerHit"))
public class ShipKillerHit extends CombatRoundKey {

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
     * The UUID of the damage dealer - which class of damage dealer hits, depends on the {@link CombatRoundKey#getCombatPhase()}.
     */
    @NotNull
    @Nonnull
    @Convert(converter = UUIDConverter.class)
    private UUID damageDealer;

    /**
     * The distance of this shot.
     */
    @NotNull
    @Nonnull
    @Column(nullable = false, columnDefinition = "decimal(19, 0)")
    private BigDecimal distance;

    /**
     * The result of this salvo.
     */
    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EDamageResult result;

    /**
     * A hit log is linked with its order number. The order defines the sequence of occurrence.
     */
    @Nonnull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idHitLog", referencedColumnName = "idHitLog")
    @Column(name = "orderNo")
    @CollectionTable(name = "orderedHitLog", joinColumns = @JoinColumn(name = "idShipKillerHit"))
    private final Map<HitLog, Integer> hitLogByOrderNo = new HashMap<>();

    public ShipKillerHit(@Nonnull final CombatRound combatRound,
                         @Nonnull final ECombatPhase.ECombatSubPhase combatPhase) {
        super(combatRound, combatPhase);


    }

    public ShipKillerHit() {
    }
}
