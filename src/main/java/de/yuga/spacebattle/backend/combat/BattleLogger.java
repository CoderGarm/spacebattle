package de.yuga.spacebattle.backend.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.enums.EDamageResult;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EHitArea;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class BattleLogger {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(BattleLogger.class);

    private final boolean logBattleResult;

    private static final String DIR = System.getProperty("java.io.tmpdir");

    private static final String PS = System.getProperty("file.separator");

    @Autowired
    public BattleLogger(@Nonnull @Value("${logging.battle-log.write:true}") final String logBattleResult) {
        Preconditions.checkNotNull(logBattleResult, "logBattleResult shouldn't be null!");

        this.logBattleResult = Boolean.parseBoolean(logBattleResult);
    }

    public void logMessage(String msg, final Long start, final Long end) {
        if (logBattleResult) {
            if (start != null && end != null) {
                final double duration = (double) (end - start) / 1000;
                LOGGER.info("\t" + msg + "\t\t - duration: " + duration + " seconds");
            } else {
                LOGGER.info("\t" + msg);
            }
        }
    }

    private BufferedWriter openStream(@Nonnull final BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        try {
            final int id = battleReport.getId();
            final Tick tick = battleReport.getTick();
            final String path = DIR + PS + "battleReports" + PS + "tick_" + tick.getId();
            final File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final FileWriter fw = new FileWriter(path + PS + "battleReport_" + id + ".txt", true);
            return new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("Universe is going down on open" + e.getMessage());
        }
    }

    private void write(@Nonnull final BufferedWriter bw, final String msg) {
        Preconditions.checkNotNull(bw, "bw must not be empty");

        try {
            bw.write(msg);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("Universe is going down on write" + e.getMessage());
        }
    }

    private void closeAndWrite(@Nonnull final BufferedWriter bw) {
        Preconditions.checkNotNull(bw, "bw must not be empty");
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("Universe is going down on close " + e.getMessage());
        }
    }


    public void logBattleResult(@Nonnull final BattleReport battleReport, @Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
        Preconditions.checkNotNull(battleResult, "battleResult shouldn't be null!");

        if (!logBattleResult) {
            return;
        }

        final BufferedWriter bw = openStream(battleReport);
        if (bw == null) {
            LOGGER.info("Battle result could not be logged.");
            return;
        }

        final Cage cage = battleResult.getCage();
        final FleetClash fleetClash = battleResult.getFleetClash();
        final Set<WarShip> losses = battleResult.getLosses();
        final List<FleetRoundState> allRoundStates = battleResult.getRoundStates();
        final List<MovementAction> allMovements = battleResult.getMovements();
        final List<BeamVolley> allBeamVolleys = battleResult.getBeamVolleys();
        final List<MissileSalvo> allMissileSalvos = battleResult.getMissileSalvos();

        final Map<CombatRound, List<FleetRoundState>> statesByRound = allRoundStates.stream()
                .collect(Collectors.groupingBy(FleetRoundState::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<MovementAction>> movementsByRound = allMovements.stream()
                .collect(Collectors.groupingBy(MovementAction::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<BeamVolley>> beamVolleyByRound = allBeamVolleys.stream()
                .collect(Collectors.groupingBy(BeamVolley::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<CombatRound, List<MissileSalvo>> missileSalvosByRound = allMissileSalvos.stream()
                .collect(Collectors.groupingBy(MissileSalvo::getCombatRound,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<CombatRound> combatRounds = statesByRound.keySet().stream().sorted(CombatRound::compareTo).collect(Collectors.toList());
        final Iterator<CombatRound> it = combatRounds.iterator();
        while (it.hasNext()) {
            final CombatRound combatRound = it.next();

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
            final List<MovementAction> movementActions = movementsByRound.computeIfAbsent(combatRound, k -> new ArrayList<>());

            final boolean somethingHappens = !movementActions.isEmpty() || !hitLogsOfCombatRound.isEmpty() || !beamVolleys.isEmpty() || !missileSalvos.isEmpty();
            if (somethingHappens) {
                logMessageWithPretendingCombatRound(bw, combatRound, "Combat round in result");
            }
            for (final ECombatPhase combatPhase : combatPhases) {
                final List<ECombatPhase.ECombatSubPhase> combatSubPhases = ECombatPhase.ECombatSubPhase.getByCombatPhase(combatPhase);
                for (final ECombatPhase.ECombatSubPhase combatSubPhase : combatSubPhases) {
                    if (somethingHappens) {
                        logMessageWithPretendingCombatRound(bw, combatRound, combatSubPhase.getTitle());
                    }
                    switch (combatSubPhase) {
                        case MOVEMENT_PHASE:
                            movementActions.forEach(m -> logMovement(bw, cage, statesByRound, m));
                            break;
                        case ELOKA_PHASE:
                            final List<MissileSalvo> destroyedWhileEloka = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            destroyedWhileEloka.forEach(d -> logElokaHitMissile(bw, d));
                            break;
                        case COUNTER_MISSILE_PHASE:
                            final List<MissileSalvo> destroyedWhileCounter = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            destroyedWhileCounter.forEach(d -> logCounterMissileHitMissile(bw, d));
                            break;
                        case MISSILE_MOVEMENT_PHASE:
                            final List<MissileSalvo> missileMovement = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            missileMovement.forEach(m -> logHandleMissileMovement(bw, m));
                            break;
                        case BEAM_FIRE_INCOMING_PHASE:
                            final List<BeamVolley> appliedBeams = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            final Map<BeamVolley, List<HitLog>> hitLogByBeamVolley = appliedBeams.stream()
                                    .map(volley -> hitLogsOfCombatRound.stream().filter(hitLog -> hitLog.getDamageDealer().equals(volley)).collect(Collectors.toList()))
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.groupingBy(hitLog -> (BeamVolley) hitLog.getDamageDealer(),
                                            Collectors.mapping(Function.identity(), Collectors.toList())));

                            appliedBeams.forEach(beamVolley -> logBeamVolleyHit(bw, beamVolley, hitLogByBeamVolley.computeIfAbsent(beamVolley, k -> new ArrayList<>())));
                            break;
                        case MISSILE_FIRE_INCOMING_PHASE:
                            final List<MissileSalvo> detonatedMissiles = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            final Map<MissileSalvo, List<HitLog>> hitLogByMissileSalvo = detonatedMissiles.stream()
                                    .map(volley -> hitLogsOfCombatRound.stream().filter(hitLog -> hitLog.getDamageDealer().equals(volley)).collect(Collectors.toList()))
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.groupingBy(hitLog -> (MissileSalvo) hitLog.getDamageDealer(),
                                            Collectors.mapping(Function.identity(), Collectors.toList())));

                            detonatedMissiles.forEach(missileSalvo -> logMissileDetonation(bw, missileSalvo, hitLogByMissileSalvo.computeIfAbsent(missileSalvo, k -> new ArrayList<>())));
                            break;
                        case BEAM_FIRE_PHASE:
                            final List<BeamVolley> releasedBeamVolleys = beamVolleys.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            releasedBeamVolleys.forEach(r -> logBeamVolleyRelease(bw, r));
                            break;
                        case MISSILE_FIRE_PHASE:
                            final List<MissileSalvo> releasedMissileSalvos = missileSalvos.stream().filter(m -> combatSubPhase == m.getCombatSubPhase()).collect(Collectors.toList());
                            releasedMissileSalvos.forEach(r -> logMissileRelease(bw, r));
                            break;
                    }
                }
            }
            if (somethingHappens) {
                logCombatRoundState(bw, combatRound, !it.hasNext());
            }
        }
        logLosses(bw, losses, fleetClash);
        closeAndWrite(bw);
    }

    private void logLosses(@Nonnull final BufferedWriter bw, final Set<WarShip> losses, final FleetClash fleetClash) {
        final StringBuilder sb = new StringBuilder();
        if (!losses.isEmpty()) {
            final FleetOrbit clashOrbit = fleetClash.getOrbit();
            String pos = "";
            final StarSystem system = clashOrbit.getSystem();
            if (system != null) {
                pos += "system " + system.getName();
            }
            final Orbit orbit = clashOrbit.getOrbit();
            if (StringUtils.isBlank(pos) && orbit != null) {
                pos += " orbital coordinates " + orbit;
            }
            sb.append("Losses of battle at " + pos + "\n");
        }
        losses.forEach(loss -> logLoss(loss, sb));
        write(bw, sb.toString());
    }

    private void logMessageWithPretendingCombatRound(@Nonnull final BufferedWriter bw, final CombatRound currentCombatRound, final String msg) {
        final String x = "#" + currentCombatRound.getNo() + ": " + msg;
        write(bw, x);
    }

    private void logCombatRoundState(@Nonnull final BufferedWriter bw, final CombatRound currentCombatRound, final boolean isDone) {
        final String msg = "current round '" + currentCombatRound.getNo() + "' - " + (isDone ? " ends battle " : " goes on");
        write(bw, msg);
    }

    @SuppressWarnings("unused")
    private void logMessage(@Nonnull final BufferedWriter bw, final String msg) {
        write(bw, msg);
    }

    private void logHandleMissileMovement(@Nonnull final BufferedWriter bw, final MissileSalvo volley) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Orbit newPos = volley.getPosition();
        final int amount = volley.getMissileSalvoHealthState().getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum();
        final Orbit targetPosition = volley.getTargetPosition();
        final Distance currentDistance = targetPosition.getDistance(newPos);
        final Distance rangePerCombatRound = volley.getRangePerCombatRound();
        final int toTravel = DistanceCalculator.getCombatRoundsToTravel(currentDistance, rangePerCombatRound);
        final String distanceAsString = currentDistance.toString();
        final String msg = "#" + combatRound.getNo() + " missile salvo " + volley.getUuid() + " of " + actor.getName() + " containing of " + amount + " and still have to travel " + toTravel + " ticks over " + distanceAsString + ".";
        write(bw, msg);
    }

    private void logMissileRelease(@Nonnull final BufferedWriter bw, final MissileSalvo volley) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Fleet target = volley.getTarget();
        final String distanceString = volley.getInitialDistance().toString();
        final Map<Missile, Integer> currentAmountByType = volley.getMissileSalvoHealthState().getCurrentAmountByType();
        final int amount = currentAmountByType.values().stream().mapToInt(Integer::intValue).sum();
        final String msg = "#" + combatRound.getNo() + " release missile salvo " + volley.getUuid() + " from " + actor.getName() + " against " + target.getName() + " with " + amount + " missiles over " + distanceString;
        write(bw, msg);
    }

    private void logBeamVolleyRelease(@Nonnull final BufferedWriter bw, final BeamVolley volley) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Fleet target = volley.getTarget();
        final int amount = volley.getFiredShots().size();
        final String distanceString = volley.getInitialDistance().toString();
        final String msg = "#" + combatRound.getNo() + " release beam volley " + volley.getUuid() + " from " + actor.getName() + " attacks " + target.getName() + " with " + amount + " shots over " + distanceString;
        write(bw, msg);
    }

    private void logMissileDetonation(@Nonnull final BufferedWriter bw, final MissileSalvo volley, final List<HitLog> hitLogs) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Fleet target = volley.getTarget();
        final EDamageResult result = volley.getResult();

        final StringBuilder sb = new StringBuilder();
        final String msg = "#" + combatRound.getNo() + " missile salvo " + volley.getUuid() + " from " + actor.getName() + " attacks " + target.getName() + " and " + result + "\n";
        sb.append(msg);
        hitLogs.forEach(hitLog -> generateHitLogMessage(hitLog, sb));
        hitLogs.stream()
                .filter(hitLog -> !hitLog.isAlive() || !hitLog.isFightingCapable())
                .map(HitLog::getWarshipHealthState)
                .map(WarshipHealthState::getWarShip)
                .forEach(warShip -> logLoss(warShip, sb));
        write(bw, sb.toString());
    }

    private void logBeamVolleyHit(@Nonnull final BufferedWriter bw, final BeamVolley volley, final List<HitLog> hitLogs) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Fleet target = volley.getTarget();
        final Distance distance = volley.getInitialDistance();
        final EDamageResult result = volley.getResult();

        final StringBuilder sb = new StringBuilder();
        final String msg = "#" + combatRound.getNo() + " beam volley " + volley.getUuid() + " from " + actor.getName() + " attacks " + target.getName() + " hits over " + distance.toString() + " and " + result + "\n";
        sb.append(msg);
        hitLogs.forEach(hitLog -> generateHitLogMessage(hitLog, sb));
        hitLogs.stream()
                .filter(hitLog -> !hitLog.isAlive() || !hitLog.isFightingCapable())
                .map(HitLog::getWarshipHealthState)
                .map(WarshipHealthState::getWarShip)
                .forEach(warShip -> logLoss(warShip, sb));
        write(bw, sb.toString());
    }

    private void logLoss(final WarShip warShip, final StringBuilder sb) {
        final String msg = "\tLoss of " + warShip.getName() + " of user " + warShip.getShipClass().getOwner().getUsername() + "\n";
        sb.append(msg);
    }

    private void generateHitLogMessage(final HitLog hitLog, final StringBuilder sb) {
        final WarshipHealthState warshipHealthState = hitLog.getWarshipHealthState();
        final long damageValue = hitLog.getDamageValue();
        final int state = hitLog.getState();
        final EHitArea attackedPart = hitLog.getAttackedPart();
        final boolean isAlive = hitLog.isAlive();
        final boolean isFightingCapable = hitLog.isFightingCapable();
        final WarShip warShip = warshipHealthState.getWarShip();
        final String killMarker = isAlive ? (isFightingCapable ? "\tHit" : "\tFinal hit") : "\tKill hit";
        sb.append(killMarker + " at " + warShip.getName() + " of user " + warShip.getShipClass().getOwner().getUsername() + " onto " + attackedPart + " with damage of " + damageValue + " current state: " + state
                + "\n\t\t " + warshipHealthState.asString() + "\n");

    }

    private void logMovement(@Nonnull final BufferedWriter bw, final Cage cage, final Map<CombatRound, List<FleetRoundState>> statesByRound, final MovementAction ma) {
        final CombatRound combatRound = ma.getCombatRound();

        final List<FleetRoundState> fleetRoundStates = statesByRound.get(combatRound);
        final Fleet actor = ma.getActor();
        final Fleet fleetOne = cage.getFleetOne();
        final Fleet fleetTwo = cage.getFleetTwo();
        final Fleet target = actor.getId() != fleetOne.getId() ? fleetOne : fleetTwo;
        final FleetRoundState targetState = fleetRoundStates.stream()
                .filter(fleetRoundState -> fleetRoundState.isEqualsByFleetAndRound(combatRound, target))
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.info("There is no fleet state for idFleet '" + target.getId() + "'.");
                    return new NotifyWebUserException("No state present - please call the administrator.");
                });

        final Orbit targetsPosition = targetState.getPosition();

        final EMovementType movementType = ma.getMovementType();
        final Orbit origin = ma.getOrigin();
        final Orbit interimDestination = ma.getInterimDestination();
        final Orbit destination = ma.getDestination();
        final String distanceAsString = stringify(interimDestination.getDistance(destination));
        final String distanceToTargetAsString = stringify(interimDestination.getDistance(targetsPosition));
        final String moveDist = stringify(origin.getDistance(interimDestination));
        final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " moves about " + moveDist +
                ", distance to destination " + distanceAsString +
                ", distance to target " + distanceToTargetAsString +
                " with the plan to " + movementType;
        write(bw, msg);
    }

    private static String stringify(Distance distance) {

        EDistanceMetric distanceMetric = EDistanceMetric.LS;
        BigDecimal coordinateInMetric = distance.getCoordinateInMetric(distanceMetric);
        final int compareTo = coordinateInMetric.compareTo(BigDecimal.ONE);
        if (compareTo == 0) {
            return " no distance";
        } else if (compareTo < 0) {
            distanceMetric = EDistanceMetric.KM;
            coordinateInMetric = distance.getCoordinateInMetric(distanceMetric);
        }
        return coordinateInMetric + " " + distanceMetric;
    }

    private void logCounterMissileHitMissile(@Nonnull final BufferedWriter bw, final MissileSalvo volley) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final MissileSalvoHealthState missileSalvoHealthState = volley.getMissileSalvoHealthState();
        final Map<Missile, Integer> lossesByType = missileSalvoHealthState.getLossesByType();
        final StringBuilder sb = new StringBuilder();
        lossesByType.forEach((missile, lostAmount) -> {
            final Integer leftOver = missileSalvoHealthState.getCurrentAmountByType().get(missile);
            final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " shots down " + volley.getUuid() + " and hits " + lostAmount + " missiles of " + missile.getName(Translation.DEFAULT_LANGUAGE) + " - left over " + leftOver + " missiles.";
            sb.append(msg);
        });
        if (StringUtils.isBlank(sb)) {
            return;
        }
        write(bw, sb.toString());
    }

    private void logElokaHitMissile(@Nonnull final BufferedWriter bw, final MissileSalvo volley) {
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Fleet target = volley.getTarget();
        final MissileSalvoHealthState missileSalvoHealthState = volley.getMissileSalvoHealthState();
        final Map<Missile, Integer> lossesByType = missileSalvoHealthState.getLossesByType();
        final StringBuilder sb = new StringBuilder();
        lossesByType.forEach((missile, lostAmount) -> {
            final Integer leftOver = missileSalvoHealthState.getCurrentAmountByType().get(missile);
            final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " eloka down " + volley.getUuid() + " and hits " + lostAmount + " missiles of " + missile.getName(Translation.DEFAULT_LANGUAGE) + " from " + target.getName() + " - left over " + leftOver + " missiles.";
            sb.append(msg);
        });
        if (StringUtils.isBlank(sb)) {
            return;
        }
        write(bw, sb.toString());
    }
}
