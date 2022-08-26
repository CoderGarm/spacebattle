package de.yuga.spacebattle.backend.entities.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.BattleResult;
import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.HitLog;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.converter.CombatRoundConverter;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.*;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "BattleReport.findByIdWithAllData",
                query = "SELECT r FROM BattleReport r LEFT JOIN r.participatingUsers u ON (u.id = :idUser) WHERE r.id = :idBattleReport"),
        @NamedQuery(name = "BattleReport.countAllWithUser",
                query = "SELECT COUNT(r) FROM BattleReport r LEFT JOIN r.participatingUsers u ON (u.id = :idUser)"),
})
@Entity
@Table(name = "battleReport")
@AttributeOverride(name = "id", column = @Column(name = "idBattleReport"))
public class BattleReport extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTick")
    private Tick tick;

    /**
     * The current combat round.<br>
     * A volley of direct weapons will hit in the same weapon.
     */
    @NotNull
    @Nonnull
    @Convert(converter = CombatRoundConverter.class)
    private CombatRound lastRound;

    /**
     * The place to be.
     */
    @Nonnull
    @NotNull
    @Embedded
    private FleetOrbit venue;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "participatingUsers",
            joinColumns = @JoinColumn(name = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idUser"),
            uniqueConstraints = @UniqueConstraint(name = "participatingUsers_UC", columnNames = {"idUser", "idBattleReport"}))
    private final Set<User> participatingUsers = new HashSet<>();

    /**
     * The protagonists - and the antagonists.
     */
    @Nonnull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "participatingFleets",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idFleet", referencedColumnName = "idFleet")
    )
    private final Set<Fleet> participatingFleets = new HashSet<>();

    /**
     * The movements which were done in this clash.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "movementActions",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idMovementAction", referencedColumnName = "idMovementAction")
    )
    private final Set<MovementAction> movementActions = new HashSet<>();

    /**
     * The hits against missile salvos.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "counterMissileHits",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idCounterMissileHit", referencedColumnName = "idCounterMissileHit")
    )
    private final Set<CounterMissileHit> counterMissileHits = new HashSet<>();

    /**
     * All loose off weapon action.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "releasesVolleys",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idReleasedVolley", referencedColumnName = "idReleasedVolley")
    )
    private final Set<ReleasedVolley> releasedVolleys = new HashSet<>();

    /**
     * The missile movements during this combat.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "missileMovements",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idMissileMovement", referencedColumnName = "idMissileMovement")
    )
    private final Set<MissileMovement> missileMovements = new HashSet<>();

    /**
     * All hits of ship killer weapons during this combat.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "shipKillerHits",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idShipKillerHit", referencedColumnName = "idShipKillerHit")
    )
    private final Set<ShipKillerHit> shipKillerHits = new HashSet<>();

    public BattleReport() {
    }

    public BattleReport(@Nonnull final Tick tick, @Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(tick, "tick shouldn't be null!");
        Preconditions.checkNotNull(battleResult, "fightingResult shouldn't be null!");

        this.tick = tick;
        this.venue = battleResult.getFleetClash().getOrbit();
        this.participatingUsers.addAll(battleResult.getFleetClash().getParticipatingFleets().stream().map(Fleet::getOwner).collect(Collectors.toSet()));
        this.participatingFleets.addAll(battleResult.getFleetClash().getParticipatingFleets());
        stateBattleResult(battleResult);
    }

    @Nonnull
    public CombatRound getLastRound() {
        return lastRound;
    }

    @Nonnull
    public Tick getTick() {
        return tick;
    }

    @Nonnull
    public FleetOrbit getVenue() {
        return venue;
    }

    @Nonnull
    public Set<User> getParticipatingUsers() {
        return participatingUsers;
    }

    @Nonnull
    public List<LossRole> getLossRole() {
        return shipKillerHits.stream().map(h -> h.getLossesByHit().values()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Nonnull
    public Set<Fleet> getParticipatingFleets() {
        return participatingFleets;
    }

    @Nonnull
    public Set<MovementAction> getMovementActions() {
        return movementActions;
    }

    @Nonnull
    public Set<CounterMissileHit> getCounterMissileHits() {
        return counterMissileHits;
    }

    @Nonnull
    public Set<ReleasedVolley> getReleasedVolleys() {
        return releasedVolleys;
    }

    @Nonnull
    public Set<MissileMovement> getMissileMovements() {
        return missileMovements;
    }

    @Nonnull
    public Set<ShipKillerHit> getShipKillerHits() {
        return shipKillerHits;
    }

    private void stateBattleResult(@Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(battleResult, "fightingResult shouldn't be null!");

        final List<FleetRoundState> allRoundStates = battleResult.getRoundStates();
        final List<de.yuga.spacebattle.backend.combat.dto.MovementAction> allMovements = battleResult.getMovements();
        final List<BeamVolley> allBeamVolleys = battleResult.getBeamVolleys();
        final List<MissileSalvo> allMissileSalvos = battleResult.getMissileSalvos();

        final Map<CombatRound, List<FleetRoundState>> statesByRound = allRoundStates.stream()
                .collect(Collectors.groupingBy(FleetRoundState::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<de.yuga.spacebattle.backend.combat.dto.MovementAction>> movementsByRound = allMovements.stream()
                .collect(Collectors.groupingBy(de.yuga.spacebattle.backend.combat.dto.MovementAction::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<BeamVolley>> beamVolleyByRound = allBeamVolleys.stream()
                .collect(Collectors.groupingBy(BeamVolley::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<MissileSalvo>> missileSalvosByRound = allMissileSalvos.stream()
                .collect(Collectors.groupingBy(MissileSalvo::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<CombatRound> combatRounds = statesByRound.keySet().stream().sorted(CombatRound::compareTo).collect(Collectors.toList());
        this.lastRound = Collections.max(combatRounds, CombatRound::compareTo);
        for (final CombatRound combatRound : combatRounds) {
            final List<ECombatPhase> combatPhases = Arrays.stream(ECombatPhase.values()).collect(Collectors.toList());

            final List<FleetRoundState> fleetRoundStates = statesByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());

            final List<HitLog> hitLogsOfCombatRound = fleetRoundStates.stream().map(f -> f.getFleetHealthState().getHitLogs().values())
                    .flatMap(Collection::stream)
                    .flatMap(Collection::stream)
                    // filter all hit logs from other rounds away - necessary because the fleet round states object character
                    .filter(hitLog -> hitLog.getCombatRound().compareTo(combatRound) == 0)
                    .collect(Collectors.toList());

            final List<BeamVolley> beamVolleys = beamVolleyByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            final List<MissileSalvo> missileSalvos = missileSalvosByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            final List<de.yuga.spacebattle.backend.combat.dto.MovementAction> movementActions = movementsByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            for (final ECombatPhase combatPhase : combatPhases) {
                final List<ECombatPhase.ECombatSubPhase> combatSubPhases = ECombatPhase.ECombatSubPhase.getByCombatPhase(combatPhase);
                for (final ECombatPhase.ECombatSubPhase combatSubPhase : combatSubPhases) {
                    switch (combatSubPhase) {
                        case MOVEMENT_PHASE:
                            addMovementAction(movementActions);
                            break;
                        case ELOKA_PHASE:
                        case COUNTER_MISSILE_PHASE:
                            final List<MissileSalvo> destroyedWhileEloka = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            destroyedWhileEloka.forEach(this::addCounterMissileHitMissile);
                            break;
                        case MISSILE_MOVEMENT_PHASE:
                            final List<MissileSalvo> missileMovement = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            missileMovement.forEach(this::addMissileMovement);
                            break;
                        case BEAM_FIRE_INCOMING_PHASE:
                            final List<BeamVolley> appliedBeams = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            final Map<BeamVolley, List<HitLog>> hitLogByBeamVolley = appliedBeams.stream()
                                    .map(volley -> hitLogsOfCombatRound.stream().filter(hitLog -> hitLog.getDamageDealer().equals(volley)).collect(Collectors.toList()))
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.groupingBy(hitLog -> (BeamVolley) hitLog.getDamageDealer(),
                                            Collectors.mapping(Function.identity(), Collectors.toList())));

                            appliedBeams.forEach(beamVolley -> addShipKillerHit(beamVolley, hitLogByBeamVolley.computeIfAbsent(beamVolley, k -> new ArrayList<>())));
                            break;
                        case MISSILE_FIRE_INCOMING_PHASE:
                            final List<MissileSalvo> detonatedMissiles = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            final Map<MissileSalvo, List<HitLog>> hitLogByMissileSalvo = detonatedMissiles.stream()
                                    .map(volley -> hitLogsOfCombatRound.stream().filter(hitLog -> hitLog.getDamageDealer().equals(volley)).collect(Collectors.toList()))
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.groupingBy(hitLog -> (MissileSalvo) hitLog.getDamageDealer(),
                                            Collectors.mapping(Function.identity(), Collectors.toList())));

                            detonatedMissiles.forEach(missileSalvo -> addShipKillerHit(missileSalvo, hitLogByMissileSalvo.computeIfAbsent(missileSalvo, k -> new ArrayList<>())));
                            break;
                        case BEAM_FIRE_PHASE:
                            final List<BeamVolley> releasedBeamVolleys = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            releasedBeamVolleys.forEach(this::addReleasedVolley);
                            break;
                        case MISSILE_FIRE_PHASE:
                            final List<MissileSalvo> releasedMissileSalvos = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            releasedMissileSalvos.forEach(this::addReleasedVolley);
                            break;
                    }
                }
            }
        }
    }

    private void addMovementAction(final List<de.yuga.spacebattle.backend.combat.dto.MovementAction> movementActions) {
        movementActions.forEach(ma -> this.movementActions.add(new de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction(ma)));
    }

    private void addMissileMovement(@Nonnull final MissileSalvo volley) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");

        missileMovements.add(new MissileMovement(volley));
    }

    private void addReleasedVolley(final MissileSalvo volley) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");

        releasedVolleys.add(new ReleasedVolley(volley));
    }

    private void addReleasedVolley(@Nonnull final BeamVolley volley) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");

        releasedVolleys.add(new ReleasedVolley(volley));
    }

    private void addShipKillerHit(@Nonnull final BeamVolley volley, @Nonnull final List<HitLog> hitLogs) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");
        Preconditions.checkNotNull(hitLogs, "hitLogs shouldn't be null!");

        shipKillerHits.add(new ShipKillerHit(volley, hitLogs));
    }

    private void addShipKillerHit(@Nonnull final MissileSalvo volley, @Nonnull final List<HitLog> hitLogs) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");
        Preconditions.checkNotNull(hitLogs, "hitLogs shouldn't be null!");

        shipKillerHits.add(new ShipKillerHit(volley, hitLogs));
    }

    private void addCounterMissileHitMissile(@Nonnull final MissileSalvo volley) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");

        final MissileSalvoHealthState missileSalvoHealthState = volley.getMissileSalvoHealthState();
        final Map<Missile, Integer> lossesByType = missileSalvoHealthState.getLossesByType();
        lossesByType.forEach((missile, lostAmount) -> {
            final Integer leftOver = missileSalvoHealthState.getCurrentAmountByType().get(missile);
            counterMissileHits.add(new CounterMissileHit(volley, missile, leftOver, lostAmount));
        });
    }
}
