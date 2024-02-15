package de.yuga.spacebattle.backend.entities.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.dto.HitLog;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionProfile;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.converter.CombatRoundConverter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.*;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "battleReport")
@AttributeOverride(name = "id", column = @Column(name = "idBattleReport"))
public class BattleReport extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTick")
    private Tick tick;

    @Nonnull
    @NotNull
    private String uuid;

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
     * The protagonists - and the antagonists.
     */
    @Nonnull
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "participatingFleets",
            joinColumns = @JoinColumn(name = "idBattleReport", referencedColumnName = "idBattleReport"),
            inverseJoinColumns = @JoinColumn(name = "idFleetSnapshot", referencedColumnName = "idFleetSnapshot")
    )
    private final Set<FleetSnapshot> participatingFleets = new HashSet<>();

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
        this.lastRound = Collections.max(battleResult.getCage().getRounds(), CombatRound::compareTo);
        this.venue = battleResult.getFleetClash().getOrbit();
        this.participatingFleets.addAll(battleResult.getFleetClash().getParticipatingFleets().stream().map(f -> new FleetSnapshot(this, f)).collect(Collectors.toSet()));
        this.uuid = this.createUUID();
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
    public List<LossRole> getLossRole() {
        return shipKillerHits.stream().map(h -> h.getLossesByHit().values()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Nonnull
    public Set<FleetSnapshot> getParticipatingFleets() {
        return participatingFleets;
    }

    @Nonnull
    public Set<MovementAction> getMovementActions() {
        return movementActions;
    }

    @Nonnull
    public Set<Maneuver> getManeuvers() {
        return movementActions.stream().map(MovementAction::getManeuver).collect(Collectors.toSet());
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

        final List<CombatRound> combatRounds = battleResult.getCage().getRounds();
        final List<FleetRoundState> fleetRoundStates = battleResult.getRoundStates();

        final List<CoursePlot> allCoursePlots = battleResult.getCoursePlots();
        final Map<CombatRound, List<de.yuga.spacebattle.backend.combat.dto.MovementAction>> movementsByRound = allCoursePlots.stream()
                .map(c -> c.getManeuver().getMovementActions())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(de.yuga.spacebattle.backend.combat.dto.MovementAction::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<de.yuga.spacebattle.backend.combat.maneuver.Maneuver, Maneuver> maneuvers = allCoursePlots.stream()
                .map(CoursePlot::getManeuver)
                .filter(de.yuga.spacebattle.backend.combat.maneuver.Maneuver::isValid)
                .collect(Collectors.toMap(Function.identity(), Maneuver::new));

        final Map<CombatRound, List<AuraState>> aurasByRound = new HashMap<>();
        fleetRoundStates.forEach(fleetRoundState -> {
            fleetRoundState.getAuraStates().forEach((combatRound, auraState) -> {
                final List<AuraState> orDefault = aurasByRound.getOrDefault(combatRound, new ArrayList<>());
                orDefault.add(auraState);
                aurasByRound.put(combatRound, orDefault);
            });
        });

        final List<BeamVolley> allBeamVolleys = battleResult.getBeamVolleys();
        final Map<CombatRound, List<BeamVolley>> beamVolleyByRound = allBeamVolleys.stream()
                .collect(Collectors.groupingBy(BeamVolley::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<MissileSalvo> allMissileSalvos = battleResult.getMissileSalvos();
        final Map<CombatRound, List<MissileSalvo>> missileSalvosByRound = allMissileSalvos.stream()
                .collect(Collectors.groupingBy(MissileSalvo::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<DamageDealer, List<HitLog>> hitLogsByDamageDealer = getHitLogsByDamageDealer(fleetRoundStates);

        final List<ECombatPhase> combatPhases = Arrays.stream(ECombatPhase.values()).collect(Collectors.toList());
        for (final CombatRound combatRound : combatRounds) {
            // fixme remove the combat-round-addiction from stating the combat

            final List<BeamVolley> beamVolleys = beamVolleyByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            final List<MissileSalvo> missileSalvos = missileSalvosByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            final List<de.yuga.spacebattle.backend.combat.dto.MovementAction> movementActions = movementsByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            final List<AuraState> auraStates = aurasByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());
            for (final ECombatPhase combatPhase : combatPhases) {
                final List<ECombatPhase.ECombatSubPhase> combatSubPhases = ECombatPhase.ECombatSubPhase.getByCombatPhase(combatPhase);
                for (final ECombatPhase.ECombatSubPhase combatSubPhase : combatSubPhases) {
                    switch (combatSubPhase) {
                        case MOVEMENT_PHASE:
                            addMovementAction(movementActions, auraStates, maneuvers);
                            break;
                        case ELOKA_PHASE:
                        case COUNTER_MISSILE_PHASE:
                        case MISSILE_MOVEMENT_PHASE:
                        case MISSILE_FIRE_PHASE:
                            missileSalvos.forEach(this::addCounterMissileHitMissile);
                            missileSalvos.forEach(this::addMissileMovement);
                            missileSalvos.forEach(this::addReleasedVolley);
                            break;
                        case BEAM_FIRE_INCOMING_PHASE:
                            final List<BeamVolley> appliedBeams = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            appliedBeams.forEach(beamVolley -> addShipKillerHit(beamVolley, hitLogsByDamageDealer.computeIfAbsent(beamVolley, k -> new ArrayList<>())));
                            break;
                        case MISSILE_FIRE_INCOMING_PHASE:
                            final List<MissileSalvo> detonatedMissiles = missileSalvos.stream().filter(m -> !m.getAppliedDamage().isEmpty()).collect(Collectors.toList());
                            detonatedMissiles.forEach(missileSalvo -> addShipKillerHit(missileSalvo, hitLogsByDamageDealer.computeIfAbsent(missileSalvo, k -> new ArrayList<>())));
                            break;
                        case BEAM_FIRE_PHASE:
                            final List<BeamVolley> releasedBeamVolleys = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            releasedBeamVolleys.forEach(this::addReleasedVolley);
                            break;
                    }
                }
            }
        }
    }

    @Nonnull
    private static Map<DamageDealer, List<HitLog>> getHitLogsByDamageDealer(@Nonnull final List<FleetRoundState> fleetRoundStates) {
        Preconditions.checkNotNull(fleetRoundStates, "fleetRoundStates must not be empty");

        final Map<DamageDealer, List<HitLog>> hitLogsByDamageDealer = new HashMap<>();
        fleetRoundStates.forEach(fleetRoundState -> {
            fleetRoundState.getFleetHealthState().getHitLogs().values().stream().flatMap(Collection::stream).forEach(hitLog -> {
                final List<HitLog> orDefault = hitLogsByDamageDealer.getOrDefault(hitLog.getDamageDealer(), new ArrayList<>());
                orDefault.add(hitLog);
                hitLogsByDamageDealer.put(hitLog.getDamageDealer(), orDefault);
            });
        });
        return hitLogsByDamageDealer;
    }

    private void addMovementAction(@Nonnull final List<de.yuga.spacebattle.backend.combat.dto.MovementAction> movementActions,
                                   @Nonnull final List<AuraState> auraStates,
                                   @Nonnull final Map<de.yuga.spacebattle.backend.combat.maneuver.Maneuver, Maneuver> maneuvers) {
        Preconditions.checkNotNull(movementActions, "movementActions must not be empty");
        Preconditions.checkNotNull(auraStates, "auraStates must not be empty");
        Preconditions.checkNotNull(maneuvers, "maneuvers must not be empty");

        for (final de.yuga.spacebattle.backend.combat.dto.MovementAction movementAction : movementActions) {
            final Fleet actor = movementAction.getActor();
            final AuraState auraState = auraStates.stream().filter(a -> a.getActor().equals(actor)).findFirst().orElseThrow();
            this.movementActions.add(new de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction(maneuvers.get(movementAction.getManeuver()), movementAction, auraState));
        }
    }

    private void addMissileMovement(@Nonnull final MissileSalvo volley) {
        Preconditions.checkNotNull(volley, "volley shouldn't be null!");

        volley.getMotionProfile().stream().sorted()
                .forEach(motionProfile -> missileMovements.add(new MissileMovement(volley, motionProfile)));
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
        final List<MissileAmmunitionProfile> lossesByType = missileSalvoHealthState.getLosses();
        lossesByType.forEach(missileAmmunitionProfile -> missileAmmunitionProfile.getAmmunitionState().getRemainingShots()
                .forEach((missile, amount) -> counterMissileHits.add(new CounterMissileHit(volley, missileAmmunitionProfile, missile, amount))));
    }

    public void changeParticipant(@Nonnull final SharedBattleReport sharedBattleReport, @Nonnull final User toRemove, @Nonnull final Owner pirate) {
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");
        Preconditions.checkNotNull(toRemove, "toRemove must not be empty");
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        sharedBattleReport.getParticipatingUsers().remove(toRemove);
        sharedBattleReport.getParticipatingUsers().add(pirate);

        sharedBattleReport.getSharedWithUsers().remove(toRemove);

        final Set<LossRole> impactedRoles = getLossRole().stream().filter(l -> toRemove.equals(l.getHumanOwner())).collect(Collectors.toSet());
        impactedRoles.forEach(l -> l.setOwner(pirate));

        participatingFleets.stream().filter(f -> f.getOwner().equals(toRemove)).forEach(f -> f.setOwner(pirate));
    }

    @Nonnull
    public Set<Owner> getParticipatingUsers() {
        return getParticipatingFleets().stream().map(FleetSnapshot::getOwner).collect(Collectors.toSet());
    }

    @Nonnull
    private String createUUID() {
        final String hashCode = "" + new HashCodeBuilder(17, 37).append(tick).append(participatingFleets).toHashCode();
        return UUID.nameUUIDFromBytes(hashCode.getBytes()).toString();
    }

    @Nonnull
    public String getUUID() {
        return uuid;
    }
}
