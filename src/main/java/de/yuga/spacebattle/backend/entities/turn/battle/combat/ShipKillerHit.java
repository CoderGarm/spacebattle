package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.converter.UUIDConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.turn.battle.LossRole;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "shipKillerHit")
@AttributeOverride(name = "id", column = @Column(name = "idShipKillerHit"))
public class ShipKillerHit extends CombatRoundKey {

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
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderColumn(name = "orderNo")
    @JoinTable(name = "orderedHitLog",
            joinColumns = @JoinColumn(name = "idShipKillerHit"),
            inverseJoinColumns = @JoinColumn(name = "idHitLog"))
    private final List<HitLog> hitLogs = new ArrayList<>();

    /**
     * If the hit results in a destroyed ship, this will be logged here.
     */
    @Nonnull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idHitLog", referencedColumnName = "idHitLog", updatable = false)
    @CollectionTable(name = "lossesByHit", joinColumns = @JoinColumn(name = "idShipKillerHit"))
    private final Map<HitLog, LossRole> lossesByHit = new HashMap<>();

    public ShipKillerHit() {
    }

    public ShipKillerHit(@Nonnull final BeamVolley volley,
                         @Nonnull final List<de.yuga.spacebattle.backend.combat.dto.HitLog> hitLogs) {
        super(volley.getCombatRound(), volley.getCombatSubPhase());
        Preconditions.checkNotNull(hitLogs, "hitLogs shouldn't be null!");
        Preconditions.checkState(volley.getResult() != null, "volley result shouldn't be null!");

        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.distance = volley.getInitialDistance();
        this.result = volley.getResult();

        final Map<de.yuga.spacebattle.backend.combat.dto.HitLog, LossRole> lossesByHitLog = hitLogs.stream()
                .filter(hitLog -> !hitLog.isAlive() || !hitLog.isFightingCapable())
                .collect(Collectors.toMap(Function.identity(), h -> new LossRole(h.getWarshipHealthState().getWarShip())));

        generateHitLogs(volley.getUuid(), hitLogs, lossesByHitLog, volley.getCombatSubPhase());
    }

    public ShipKillerHit(@Nonnull final MissileSalvo volley, @Nonnull final List<de.yuga.spacebattle.backend.combat.dto.HitLog> hitLogs) {
        super(volley.getCombatRound(), volley.getCombatSubPhase());
        Preconditions.checkNotNull(hitLogs, "hitLogs shouldn't be null!");
        Preconditions.checkState(volley.getResult() != null, "volley result shouldn't be null!");

        this.actor = volley.getActor();
        this.target = volley.getTarget();
        this.damageDealer = volley.getUuid();
        this.distance = volley.getInitialDistance();
        this.result = volley.getResult();

        final Map<de.yuga.spacebattle.backend.combat.dto.HitLog, LossRole> lossesByHitLog = hitLogs.stream()
                .filter(hitLog -> !hitLog.isAlive() || !hitLog.isFightingCapable())
                .collect(Collectors.toMap(Function.identity(), h -> new LossRole(h.getWarshipHealthState().getWarShip())));

        generateHitLogs(volley.getUuid(), hitLogs, lossesByHitLog, volley.getCombatSubPhase());
    }

    private void generateHitLogs(@Nonnull final UUID damageDealerId,
                                 @Nonnull final List<de.yuga.spacebattle.backend.combat.dto.HitLog> hitLogs,
                                 @Nonnull final Map<de.yuga.spacebattle.backend.combat.dto.HitLog, LossRole> lossesByHitLog,
                                 @Nonnull final ECombatPhase.ECombatSubPhase combatSubPhase) {
        Preconditions.checkNotNull(damageDealerId, "damageDealerId shouldn't be null!");
        Preconditions.checkNotNull(hitLogs, "hitLogs shouldn't be null!");
        Preconditions.checkNotNull(lossesByHitLog, "lossesByHitLog shouldn't be null!");
        Preconditions.checkNotNull(combatSubPhase, "combatSubPhase shouldn't be null!");

        for (de.yuga.spacebattle.backend.combat.dto.HitLog h : hitLogs) {
            final HitLog hitLog = new HitLog(damageDealerId, h, combatSubPhase);
            this.hitLogs.add(hitLog);
            final LossRole lossRole = lossesByHitLog.get(h);
            if (lossRole != null) {
                lossesByHit.put(hitLog, lossRole);
            }
        }
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
    public BigDecimal getDistance() {
        return distance;
    }

    @Nonnull
    public EDamageResult getResult() {
        return result;
    }

    @Nonnull
    public List<HitLog> getHitLogs() {
        return hitLogs;
    }

    @Nonnull
    public Map<HitLog, LossRole> getLossesByHit() {
        return lossesByHit;
    }
}
